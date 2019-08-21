#!/usr/bin/env bash

# Usage: ./run-all.sh numNodes numEventsPerSecond runDuration windows aggFunctions [--delete | --no-delete]

NUM_EXPECTED_DROPLETS=${1}
NUM_EVENTS_PER_SECOND=${2}
RUN_DURATION_SECONDS=${3}
WINDOW_STRING=${4}
AGG_STRING=${5}

DELETE_AFTER=""
if [[ "$*" == *delete* ]]
then
    DELETE_AFTER="--delete"
fi
if [[ "$*" == *--no-delete* ]]
then
    DELETE_AFTER="--no-delete"
fi

KNOWN_HOSTS_FILE=/tmp/known_hosts

THIS_FILE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
BASE_DIR=$(cd "$THIS_FILE_DIR/.." && pwd)
RUN_FILES_DIR="$BASE_DIR/benchmark-runs/$(date +"%Y-%m-%d-%H%M")_$NUM_EXPECTED_DROPLETS-nodes_$NUM_EVENTS_PER_SECOND-events_$RUN_DURATION_SECONDS-seconds"

function get_droplet_list {
    local FORMAT=${1}
    local TAG_NAME=${2}
    doctl compute droplet list --format="$FORMAT" --tag-name="$TAG_NAME" --no-header
}

function get_all_ips {
    get_droplet_list "PublicIPv4"
}

function get_all_names {
    get_droplet_list "Name"
}

function ssh_cmd {
    local ip=${1}
    local cmd=${2}
    ssh -o UserKnownHostsFile=${KNOWN_HOSTS_FILE} "root@$ip" "$cmd 2>&1"
}

function run_droplet {
    local IP=${1}
    local NAME=${2}
    ssh_cmd ${IP} "~/run.sh" &> "$RUN_FILES_DIR/$NAME.log"
}

function upload_run_params() {
    local IP=${1}
    local BENCHMARK_ARGS="${@:2}"
    ssh_cmd ${IP} "echo \"export BENCHMARK_ARGS=\\\"${BENCHMARK_ARGS}\\\"\" >> ~/benchmark_env"
}

function check_ready {
    local result=$(ssh_cmd ${1} "ls")
    if [[ ${result} == *"run.sh"* ]]; then
        echo "ready"
    fi
}

#########################
# ACTUAL CODE THAT IS RUN
#########################

if [[ -z "$NUM_EXPECTED_DROPLETS" ]] || ! [[ ${NUM_EXPECTED_DROPLETS} =~ ^[0-9]+$ ]]; then
    echo "Need to specify expected number of nodes. Got: '$NUM_EXPECTED_DROPLETS'"
    exit 1
fi

mkdir -p ${RUN_FILES_DIR}
echo "Writing logs to $RUN_FILES_DIR"
echo

echo "Getting IPs..."
ALL_IPS=($(get_all_ips))
while [[ ${#ALL_IPS[@]} -lt ${NUM_EXPECTED_DROPLETS} ]]; do
    let "difference = ${NUM_EXPECTED_DROPLETS} - ${#ALL_IPS[@]}"
    echo -ne "\rWaiting for $difference more node(s) to get an IP..."
    sleep 5
    ALL_IPS=($(get_all_ips))
done
echo
echo "All IPs (${#ALL_IPS[@]}): ${ALL_IPS[@]}"
echo

echo "Adding IPs to $KNOWN_HOSTS_FILE"
echo "This may take a while if the nodes were just created."
> ${KNOWN_HOSTS_FILE}
for ip in ${ALL_IPS[@]}; do
    SCAN_OUTPUT=""
    echo "Adding $ip"
    while [[ ${SCAN_OUTPUT} == "" ]]; do
        SCAN_OUTPUT=$(ssh-keyscan -H -t ecdsa-sha2-nistp256 ${ip} 2>/dev/null)
        if [[ ${SCAN_OUTPUT} == "" ]]; then
            sleep 5
        fi
    done
    echo ${SCAN_OUTPUT} >> ${KNOWN_HOSTS_FILE}
done
echo

echo "Waiting for node setup to complete..."
READY_IPS=()
while [[ ${#READY_IPS[@]} -lt ${NUM_EXPECTED_DROPLETS} ]]; do
    UNREADY_IPS=($(echo ${ALL_IPS[@]} ${READY_IPS[@]} | tr ' ' '\n' | sort | uniq -u | tr '\n' ' '))
    echo -ne "\rWaiting for ${#UNREADY_IPS[@]} more node(s) to become ready..."

    for ip in ${UNREADY_IPS[@]}; do
        READY_STATUS=$(check_ready ${ip})
        if [[ ${READY_STATUS} == "ready" ]]; then
            READY_IPS+=(${ip})
        fi
    done
    if [[ ${#READY_IPS[@]} -lt ${NUM_EXPECTED_DROPLETS} ]]; then
        sleep 10
    fi
done
echo -e "\n"


echo "Setup done. Uploading benchmark arguments on all nodes."

CHILD_IPS=($(get_droplet_list "PublicIPv4" "child"))
NUM_CHILDREN=${#CHILD_IPS[@]}

ROOT_IP=$(get_droplet_list "PublicIPv4" "root")
upload_run_params $ROOT_IP $NUM_CHILDREN $WINDOW_STRING $AGG_STRING

STREAM_IPS=($(get_droplet_list "PublicIPv4" "stream"))
for i in ${!STREAM_IPS[@]}; do
    upload_run_params ${STREAM_IPS[$i]} ${NUM_EVENTS_PER_SECOND} ${RUN_DURATION_SECONDS}
done
echo

echo "Starting \`run.sh\` on all nodes."

ALL_NAMES=($(get_all_names))
for i in ${!ALL_IPS[@]}; do
    run_droplet ${ALL_IPS[$i]} ${ALL_NAMES[$i]} &
done
echo

echo "To view root logs:"
echo "tail -F $RUN_FILES_DIR/root.log"

echo
echo "Running on nodes for $RUN_DURATION_SECONDS seconds..."
sleep $(expr $RUN_DURATION_SECONDS + 30)

echo
echo "Ending script by killing all PIDs..."
for ip in ${ALL_IPS[@]}; do
    ssh_cmd ${ip} "kill -9 \$(cat /tmp/RUN_PID) > /dev/null"
done

echo "Killed all PIDs."

if [[ ${DELETE_AFTER} == "" ]]; then
    read -p "Delete all droplets? (y/N) " delete_droplets
    if [[ ${delete_droplets} == "y" ]]; then
        DELETE_AFTER="--delete"
    fi
fi

if [[ ${DELETE_AFTER} == "--delete" ]]; then
    echo "Deleting all droplets..."
    doctl compute droplet delete --force $(doctl compute droplet list --format="ID" --no-header)
fi

echo "Done."

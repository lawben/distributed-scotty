from argparse import ArgumentParser

from lib.common import logs_are_unsustainable, single_run


def single_latency_run(num_children, num_streams, num_events, duration,
                       windows, agg_functions):
    log_directory = single_run(num_children, num_streams, num_events, duration,
                               windows, agg_functions)
    return logs_are_unsustainable(log_directory), log_directory


def _latency_run(num_children, num_streams, num_events, duration,
                 windows, agg_functions):
    is_unsustainable = None
    log_directory = None
    tries = 0
    while (is_unsustainable is None or is_unsustainable) and tries < 3:
        is_unsustainable, log_directory = \
            single_latency_run(num_children, num_streams, num_events,
                               duration, windows, agg_functions)

        tries += 1
        if is_unsustainable is None:
            # Error was very different to rest of nodes.
            print(" '--> Result inconclusive, running again...")
        if is_unsustainable:
            # Error was very different to rest of nodes.
            print(" '--> Error while running, running again...")

    if is_unsustainable is None:
        print(" '--> Result inconclusive again, counting as unsustainable.")
        return

    print(f"Latencies in dir: {log_directory}")


def run_latency(num_children, num_streams, num_events, duration,
                windows, agg_functions):
    quarter_events = num_events // 4
    print(f"Running with quarter events/s: {quarter_events}")
    _latency_run(num_children, num_streams, quarter_events,
                 duration, windows, agg_functions)

    half_events = quarter_events * 2
    print(f"Running with half events/s: {half_events}")
    _latency_run(num_children, num_streams, half_events,
                 duration, windows, agg_functions)

    three_quarter_events = quarter_events * 3
    print(f"Running with three quarter events/s: {three_quarter_events}")
    _latency_run(num_children, num_streams, three_quarter_events,
                 duration, windows, agg_functions)

    full_events = num_events
    print(f"Running with full events/s: {full_events}")
    _latency_run(num_children, num_streams, full_events,
                 duration, windows, agg_functions)


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("--num-children", type=int, required=True, dest='num_children')
    parser.add_argument("--num-streams", type=int, required=True, dest='num_streams')
    parser.add_argument("--num-events", type=int, required=True, dest="num_events")
    parser.add_argument("--duration", type=int, dest='duration', required=True)
    parser.add_argument("--windows", type=str, required=True, dest="windows")
    parser.add_argument("--agg-functions", type=str, required=True, dest="agg_functions")

    args = parser.parse_args()
    run_latency(args.num_children, args.num_streams, args.num_events,
                args.duration, args.windows, args.agg_functions)

def read_input_params():
    parser = argparse.ArgumentParser()

    # env: optional, default from environment variable "env"
    parser.add_argument(
        "--env",
        required=False,
        default=os.getenv("env") or os.getenv("ENVIRONMENT"),
        help="Environment to run the script (dev, uat, prod)",
    )

    parser.add_argument(
        "--api_source",
        required=False,
        default="rest",          # we just use rest in Openshift
        help="Environment to run the script (rest, jdbc, bulk)",
    )
    parser.add_argument("--from_date", required=False, help="Start date [yyyy-mm-dd]")
    parser.add_argument("--to_date",   required=False, help="To date [yyyy-mm-dd]")

    args = vars(parser.parse_args())

    env = args["env"]
    api_source = args["api_source"]
    from_date = args["from_date"]
    to_date = args["to_date"]

    return env, from_date, to_date, datetime.date.today().strftime("%Y-%m-%d"), api_source
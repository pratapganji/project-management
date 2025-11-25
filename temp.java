def read_input_params():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--env",
        required=False,                     # ← was True
        default=os.getenv("env"),           # ← take from env var when not passed
        help="Environment to run the script (dev, uat, prod)"
    )
    parser.add_argument(
        "--api_source",
        required=False,
        default="rest",                     # we always use rest in Openshift
        help="Environment to run the script (rest, jdbc, bulk, ...)"
    )
    parser.add_argument("--from_date", required=False, help="...")
    parser.add_argument("--to_date",   required=False, help="...")

    args = vars(parser.parse_args())

    env = args["env"]                      # ← use this instead of os.getenv()
    api_source = args["api_source"]
    ...
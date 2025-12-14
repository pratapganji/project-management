#!/usr/bin/env sh
echo "fetch secret script is going to execute!.........." >&2

max_retries=5

get_secret () {
  nick="$1"
  retries=0

  while [ "$retries" -lt "$max_retries" ]; do
    RAW_OUTPUT=$(ngc getSecret --secretNickname "$nick" --csiid "$CSIID" 2>&1)

    # take last non-empty line, trim CR + whitespace
    SA_CALL_SECRET=$(echo "$RAW_OUTPUT" | sed '/^[[:space:]]*$/d' | tail -n 1 | tr -d '\r' | xargs)

    if [ -n "$SA_CALL_SECRET" ]; then
      # IMPORTANT:
      # - log to stderr so it doesn't pollute command-substitution output
      # - print ONLY the secret to stdout
      echo "Secret retrieved successfully for nickname=$nick" >&2
      printf '%s' "$SA_CALL_SECRET"
      return 0
    fi

    retries=$((retries+1))
    echo "Retry $retries/$max_retries failed for nickname=$nick" >&2
    sleep 10
  done

  echo "ERROR: Max retries reached for nickname=$nick" >&2
  return 1
}

# --- validate required inputs ---
if [ -z "$CSIID" ]; then
  echo "ERROR: CSIID is empty" >&2
  exit 1
fi

if [ -z "$SecretNickName" ]; then
  echo "ERROR: SecretNickName is empty" >&2
  exit 1
fi

# 1) Fetch ORAAS / Oracle secret
ORAAS_SECRET=$(get_secret "$SecretNickName") || exit 1
export SECRET_NICK_NAME="$ORAAS_SECRET"
echo "Exported: SECRET_NICK_NAME (length=${#SECRET_NICK_NAME})" >&2

# 2) Fetch Impala secret ONLY if nickname is provided
if [ -n "$SecretImpalaNickName" ]; then
  IMPALA_SECRET=$(get_secret "$SecretImpalaNickName") || exit 1
  export SECRET_IMPALA_NICK_NAME="$IMPALA_SECRET"
  echo "Exported: SECRET_IMPALA_NICK_NAME (length=${#SECRET_IMPALA_NICK_NAME})" >&2
else
  echo "SecretImpalaNickName not set -> skipping IMPALA secret fetch" >&2
fi
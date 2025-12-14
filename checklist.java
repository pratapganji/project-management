#!/usr/bin/env sh
echo "fetch secret script is going to execute!.........."

max_retries=5

get_secret () {
  nick="$1"
  retries=0

  while [ "$retries" -lt "$max_retries" ]; do
    RAW_OUTPUT=$(ngc getSecret --secretNickname "$nick" --csiid "$CSIID" 2>&1)

    SA_CALL_SECRET=$(echo "$RAW_OUTPUT" | sed '/^[[:space:]]*$/d' | tail -n 1 | tr -d '\r' | xargs)

    if [ -n "$SA_CALL_SECRET" ]; then
      echo "Secret retrieved successfully for nickname=$nick"
      echo "$SA_CALL_SECRET"
      return 0
    fi

    retries=$((retries+1))
    echo "Retry $retries/$max_retries failed for nickname=$nick"
    sleep 10
  done

  echo "ERROR: Max retries reached for nickname=$nick"
  return 1
}

# --- validate required inputs ---
if [ -z "$CSIID" ]; then
  echo "ERROR: CSIID is empty"
  exit 1
fi

if [ -z "$SecretNickName" ]; then
  echo "ERROR: SecretNickName is empty"
  exit 1
fi

# 1) Fetch ORAAS/Oracle secret
ORAAS_SECRET=$(get_secret "$SecretNickName") || exit 1
export SECRET_NICK_NAME="$ORAAS_SECRET"

# 2) Fetch Impala secret ONLY if nickname is provided
if [ -n "$SecretImpalaNickName" ]; then
  IMPALA_SECRET=$(get_secret "$SecretImpalaNickName") || exit 1
  export SECRET_IMPALA_NICK_NAME="$IMPALA_SECRET"
fi

echo "Secrets exported: SECRET_NICK_NAME set, SECRET_IMPALA_NICK_NAME set=$( [ -n "$SECRET_IMPALA_NICK_NAME" ] && echo yes || echo no )"
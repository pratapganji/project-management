# ========= Base Python image (replace with Citi-approved base later) =========
FROM <PYTHON_BASE_IMAGE_FROM_INFRA>

# --------- Basic paths ---------
ENV APP_HOME=/opt/olympus-sb-user-consumption
WORKDIR ${APP_HOME}

# --------- Optional: Artifactory pip config (only if you have pip.conf) ------
# If you don't have pip.conf, you can delete the next two lines.
COPY pip.conf /etc/pip.conf
ENV PIP_TRUSTED_HOST=www.artifactoryrepository.citigroup.net

# --------- Install Python dependencies ---------------------------------------
COPY requirements.txt .
RUN python -m venv /opt/venv && \
    /opt/venv/bin/pip install --no-cache-dir -r requirements.txt
ENV PATH="/opt/venv/bin:${PATH}"

# Make sure Python can import "scripts.*"
ENV PYTHONPATH="${APP_HOME}:${PYTHONPATH}"

# --------- Copy application code & config ------------------------------------
# This will copy:
# - scripts/gen_user_consumption.py
# - scripts/config/constants.json
# - scripts/config/gen_user_consumption.json
# - any other modules
COPY . .

# Create folders used by the job (logs, data, tmp)
RUN mkdir -p ${APP_HOME}/logs ${APP_HOME}/data ${APP_HOME}/tmp

# --------- Entry point: run the main process ---------------------------------
# We call it as a module so imports like "from scripts.xxx import ..." work.
CMD ["python", "-m", "scripts.gen_user_consumption"]
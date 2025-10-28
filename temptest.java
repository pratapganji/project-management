Hi Team,

I am working for the CRM Olympus API application (Clientmeta: 1505970.crm-olympus.crm-olympus-api). We are migrating the application from HTTP to HTTPS as part of the ongoing enhancement.

While creating the VIP and WIP URLs for the application, we noticed that port 2005, used in DEV/UAT, is publicly accessible. To mitigate potential external exposure risks, I would like to block this port and instead use a port number accessible only within Citi’s internal network.

Please confirm if I can proceed with this new internal port configuration for the PROD deployment. Once approved, I will configure the same port for both VIP and WIP environments.

Thanks & Regards,
Pratap Ganji
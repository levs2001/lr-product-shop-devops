#!/bin/bash

sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072

$PUBLIC_IP_OR_DOMAIN=$(cat /home/ryanbekov/external_ip.txt)

sudo bash -c "cat <<EOF > /etc/nginx/sites-available/sonarqube
server {
    listen 80;
    server_name $PUBLIC_IP_OR_DOMAIN;

    location / {
        proxy_pass http://localhost:9000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF"

sudo ln -s /etc/nginx/sites-available/sonarqube /etc/nginx/sites-enabled/

sudo nginx -t

sudo systemctl restart nginx

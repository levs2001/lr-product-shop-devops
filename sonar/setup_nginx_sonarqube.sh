#!/bin/bash

sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072

PUBLIC_IP_OR_DOMAIN=$(cat /home/ryanbekov/external_ip.txt)

# Путь к файлу конфигурации Nginx
CONFIG_FILE="/etc/nginx/sites-available/sonarqube"

PUBLIC_IP_OR_DOMAIN=$(cat /home/ryanbekov/external_ip.txt)

# Конфигурация Nginx
CONFIG="
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
"

# Вставка конфигурации в файл
echo "$CONFIG" | sudo tee -a $CONFIG_FILE > /dev/null

sudo ln -s /etc/nginx/sites-available/sonarqube /etc/nginx/sites-enabled/

sudo systemctl restart nginx
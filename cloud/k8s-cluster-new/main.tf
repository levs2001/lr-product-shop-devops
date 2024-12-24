terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  zone                     = "ru-central1-b"
  service_account_key_file = file("/Users/lsaskov/MyProgramms/Itmo/lr-game-shop-cloud/key.json")
}

resource "yandex_kubernetes_cluster" "k8s-main" {
  folder_id          = "b1g2mc1nl38o7244uq5s"
  name               = "k8s-main"
  cluster_ipv4_range = "10.96.0.0/16"
  service_ipv4_range = "10.112.0.0/16"
  network_id         = yandex_vpc_network.network-1.id
  master {
    master_location {
      zone      = yandex_vpc_subnet.subnet-1.zone
      subnet_id = yandex_vpc_subnet.subnet-1.id
    }
    security_group_ids = [yandex_vpc_security_group.k8s-public-services.id]
    public_ip          = true
  }
  service_account_id      = "ajesod3sfigomavlc6c6"
  node_service_account_id = "ajesod3sfigomavlc6c6"
}

resource "yandex_vpc_network" "network-1" {
  folder_id = "b1g2mc1nl38o7244uq5s"
  name      = "network-1"
}

resource "yandex_vpc_subnet" "subnet-1" {
  folder_id      = "b1g2mc1nl38o7244uq5s"
  name           = "subnet-1"
  v4_cidr_blocks = ["192.168.10.0/24"]
  zone           = "ru-central1-b"
  network_id     = yandex_vpc_network.network-1.id
}


resource "yandex_kubernetes_node_group" "k8s-main-ng" {
  name        = "k8s-main-ng"
  description = "Test node group"
  cluster_id  = yandex_kubernetes_cluster.k8s-main.id
  version     = "1.28"
  instance_template {
    name        = "test-{instance.short_id}-{instance_group.id}"
    platform_id = "standard-v3"
    resources {
      cores         = 2
      core_fraction = 50
      memory        = 2
    }
    boot_disk {
      size = 64
      type = "network-ssd"
    }
    network_acceleration_type = "standard"
    network_interface {
      subnet_ids         = [yandex_vpc_subnet.subnet-1.id]
      security_group_ids = [yandex_vpc_security_group.k8s-public-services.id]
      nat                = true
    }
    scheduling_policy {
      preemptible = true
    }
    metadata = {
      "ssh-keys" = file("/Users/lsaskov/.ssh/id_ed25519_ya_k8s_meta")
    }
  }
  scale_policy {
    fixed_scale {
      size = 1
    }
  }
  deploy_policy {
    max_expansion   = 3
    max_unavailable = 1
  }
  # maintenance_policy {
  #   auto_upgrade = true
  #   auto_repair  = true
  #   maintenance_window {
  #     start_time = "22:00"
  #     duration   = "10h"
  #   }
  # }
  node_labels = {
    node-label1 = "node-value1"
  }
  # node_taints = ["taint1=taint-value1:NoSchedule"]
  labels = {
    "template-label1" = "template-value1"
  }
  allowed_unsafe_sysctls = ["kernel.msg*", "net.core.somaxconn"]
}


resource "yandex_vpc_security_group" "k8s-public-services" {
  folder_id   = "b1g2mc1nl38o7244uq5s"
  name        = "k8s-public-services"
  description = "Правила группы разрешают подключение к сервисам из интернета. Примените правила только для групп узлов."
  network_id  = yandex_vpc_network.network-1.id
  ingress {
    protocol          = "TCP"
    description       = "Правило разрешает проверки доступности с диапазона адресов балансировщика нагрузки. Нужно для работы отказоустойчивого кластера Managed Service for Kubernetes и сервисов балансировщика."
    predefined_target = "loadbalancer_healthchecks"
    from_port         = 0
    to_port           = 65535
  }
  ingress {
    protocol          = "ANY"
    description       = "Правило разрешает взаимодействие мастер-узел и узел-узел внутри группы безопасности."
    predefined_target = "self_security_group"
    from_port         = 0
    to_port           = 65535
  }
  ingress {
    protocol       = "ANY"
    description    = "Правило разрешает взаимодействие под-под и сервис-сервис. Укажите подсети вашего кластера Managed Service for Kubernetes и сервисов."
    v4_cidr_blocks = concat(yandex_vpc_subnet.subnet-1.v4_cidr_blocks)
    from_port      = 0
    to_port        = 65535
  }
  ingress {
    protocol       = "ICMP"
    description    = "Правило разрешает отладочные ICMP-пакеты из внутренних подсетей."
    v4_cidr_blocks = ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"]
  }
  ingress {
    protocol       = "TCP"
    description    = "Правило разрешает входящий трафик из интернета на диапазон портов NodePort. Добавьте или измените порты на нужные вам."
    v4_cidr_blocks = ["0.0.0.0/0"]
    from_port      = 0
    to_port        = 65535
  }
  egress {
    protocol       = "ANY"
    description    = "Правило разрешает весь исходящий трафик. Узлы могут связаться с Yandex Container Registry, Yandex Object Storage, Docker Hub и т. д."
    v4_cidr_blocks = ["0.0.0.0/0"]
    from_port      = 0
    to_port        = 65535
  }
}

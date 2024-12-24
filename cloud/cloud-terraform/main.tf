terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  zone = "ru-central1-b"
  service_account_key_file = "${file("/Users/lsaskov/MyProgramms/Itmo/lr-game-shop-cloud/key.json")}"
}

resource "yandex_compute_disk" "boot-disk-1" {
  name     = "boot-disk-1"
  type     = "network-hdd"
  zone     = "ru-central1-b"
  size     = "20"
  image_id = "fd87c0qpl9prjv5up7mc"
  folder_id = "b1g2mc1nl38o7244uq5s"
}

resource "yandex_compute_instance" "vm-1" {
  name = "terraform1"
  folder_id = "b1g2mc1nl38o7244uq5s"

  resources {
    cores  = 2
    memory = 2
  }

  boot_disk {
    disk_id = yandex_compute_disk.boot-disk-1.id
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }
  metadata = {
    user-data = "${file("/Users/lsaskov/MyProgramms/Itmo/lr-game-shop-cloud/cloud-terraform/meta.txt")}"
  }
  
  connection {
    type     = "ssh"
    user     = "lsaskov"
    host     = self.network_interface.0.nat_ip_address
    private_key = file("~/.ssh/id_ed25519")
  }

  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install -y docker.io",
      "sudo systemctl start docker",
      "sudo systemctl enable docker",
      "sudo docker run -d -p 8080:8080 levs2001/lr-product-shop:0.0.1"
    ]
  }
}

resource "yandex_vpc_network" "network-1" {
  name = "network1"
  folder_id = "b1g2mc1nl38o7244uq5s"
}

resource "yandex_vpc_subnet" "subnet-1" {
  name           = "subnet1"
  zone           = "ru-central1-b"
  network_id     = yandex_vpc_network.network-1.id
  v4_cidr_blocks = ["192.168.10.0/24"]
  folder_id = "b1g2mc1nl38o7244uq5s"
}

output "internal_ip_address_vm_1" {
  value = yandex_compute_instance.vm-1.network_interface.0.ip_address
}


output "external_ip_address_vm_1" {
  value = yandex_compute_instance.vm-1.network_interface.0.nat_ip_address
}

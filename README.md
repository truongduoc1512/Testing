# 👟 Shoe Shop E-Commerce Platform

![CI/CD Pipeline](https://github.com/truongduoc1512/shoeshop/actions/workflows/deploy.yml/badge.svg)

A robust, fully automated E-commerce web application designed for selling shoes. This project not only focuses on full-stack development but also demonstrates a modern DevOps workflow with automated CI/CD pipelines deploying directly to AWS Cloud.

## 🚀 Architecture & Technologies

**Backend & Data:**
- **Core Framework:** Java Spring Boot, Spring Security
- **ORM & Database:** Hibernate, Spring Data JPA, MySQL
- **Frontend Template:** Thymeleaf

**DevOps & Cloud Infrastructure:**
- **Containerization:** Docker & Docker Compose (eliminating environment conflicts).
- **Cloud Provider:** AWS EC2 (Ubuntu Linux).
- **CI/CD Pipeline:** GitHub Actions (Automated code pull, container cleanup, and zero-downtime deployment via SSH).

## ✨ System Highlights
- **Fully Automated Deployment:** Every push to the `main` branch triggers a workflow that automatically updates the live server on AWS without manual intervention.
- **Secure Architecture:** Implemented standard SSH Key Rotation and secured server access.
- **Scalable Design:** The application and database are strictly decoupled using Docker containers, making it easy to scale or migrate in the future.

```mermaid
graph LR
    %% Định nghĩa Style màu sắc chuẩn DevOps
    classDef dev fill:#ececff,stroke:#9370DB,stroke-width:2px,rx:10,ry:10;
    classDef github fill:#24292e,stroke:#fff,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef actions fill:#2088FF,stroke:#fff,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef aws fill:#FF9900,stroke:#232F3E,stroke-width:2px,color:#232F3E,rx:10,ry:10,stroke-dasharray: 5 5;
    classDef ec2 fill:#F58536,stroke:#232F3E,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef docker fill:#2496ED,stroke:#fff,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef spring fill:#6DB33F,stroke:#fff,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef mysql fill:#4479A1,stroke:#fff,stroke-width:2px,color:#fff,rx:8,ry:8;
    classDef step fill:#fff,stroke:#333,stroke-width:1px,rx:5,ry:5;

    %% --- Khối 1: Local ---
    subgraph P1 [1. Giai đoạn Code]
        direction LR
        Dev[💻 Developer Laptop]:::dev
    end

    %% --- Khối 2: CI/CD Tự động ---
    subgraph P2 [2. Pipeline CI/CD Tự động]
        direction TB
        Git[🐙 GitHub Repository]:::github
        GHA[⚙️ GitHub Actions]:::actions
        
        subgraph Steps [Các bước Deploy]
            direction TB
            S1[🔑 1. SSH vào EC2]:::step
            S2[⬇️ 2. Git Pull Code mới]:::step
            S3[🐳 3. Chạy Docker Compose]:::step
            S1 --> S2 --> S3
        end
    end

    %% --- Khối 3: Cloud Production ---
    subgraph P3 [3. Môi trường Vận hành]
        direction TB
        subgraph Cloud [☁️ AWS Cloud]
            class Cloud aws;
            
            subgraph Server [🖥️ EC2 Instance (Ubuntu)]
                class Server ec2;
                
                subgraph ContainerEnv [🐳 Docker Engine]
                    class ContainerEnv docker;
                    direction LR
                    
                    App[🍃 Spring Boot Backend]:::spring
                    DB[🐬 MySQL Database]:::mysql
                    
                    %% Giao tiếp nội bộ
                    App <-->|Kết nối DB| DB
                end
            end
        end
    end

    %% --- Nối các khối với nhau ---
    Dev -- "git push origin main" --> Git
    Git -- "Kích hoạt Workflow" --> GHA
    GHA --> Steps
    S3 -- "Cập nhật Container" --> ContainerEnv

    %% Căn chỉnh đường nối
    linkStyle default interpolate basis
```

## 📸 User Interface

**Home & Product List**
![Product List](https://user-images.githubusercontent.com/29988949/75882730-9ad11680-5dd6-11ea-9648-252426582a96.png)
![Categories](https://user-images.githubusercontent.com/29988949/75947593-c6dfac80-5e55-11ea-8582-bce667beb9bb.png)

**Shopping Cart**
![Cart](https://user-images.githubusercontent.com/29988949/75968115-bf35fd00-5e81-11ea-9bae-e78ff047dcfd.png)

**Checkout Flow**
![Checkout](https://user-images.githubusercontent.com/29988949/75956013-da960d80-5e6b-11ea-84b2-a0ca854ef9c9.png)
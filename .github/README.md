# GitHub Workflows Documentation

This directory contains all the GitHub Actions workflows for the Order Management Service.

## Workflows Overview

### 1. CI Pipeline (ci.yml)
- **Trigger**: Push or pull request to `main` or `develop` branches
- **Jobs**:
  - Build and Test: Compiles the application and runs all tests
  - Docker Build: Builds and tests the Docker image
  - Code Quality: Runs SonarQube analysis and OWASP Dependency Check

### 2. CD Staging Deployment (cd-staging.yml)
- **Trigger**: Push to `develop` branch
- **Jobs**:
  - Deploy to Staging: Builds and deploys the application to the staging environment
  - Health check: Verifies the deployment was successful

### 3. CD Production Deployment (cd-production.yml)
- **Trigger**: Release publication
- **Jobs**:
  - Deploy to Production: Builds and deploys the application to the production environment
  - Health check: Verifies the deployment was successful
  - Notifications: Sends success/failure notifications

### 4. Security Scan (security-scan.yml)
- **Trigger**: Scheduled weekly, push or pull request to `main` or `develop` branches
- **Jobs**:
  - Security Scan: Runs OWASP Dependency Check and SpotBugs security analysis

## Environment Variables

For the workflows to work correctly, you need to set up the following secrets in your GitHub repository:

- `DOCKERHUB_USERNAME`: Your Docker Hub username
- `DOCKERHUB_TOKEN`: Your Docker Hub access token
- `SONAR_TOKEN`: Your SonarQube token (if using SonarQube)

## Required Files

- `spotbugs-security-include.xml`: Defines which security bugs to include in the analysis
- `spotbugs-security-exclude.xml`: Defines which security bugs to exclude from the analysis

## Customization

You can customize these workflows based on your specific requirements:
- Modify trigger conditions in the `on` section
- Adjust build steps and commands
- Add or remove jobs based on your deployment strategy
- Configure notifications for workflow success/failure
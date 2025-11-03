# order-management - Helm Deployment

Order lifecycle management

## Quick Deploy

Deploy this service only:

```bash
cd deployment/helm/order-management
helm dependency update
helm install order-management . -n paklog --create-namespace
```

## Configuration

Edit `values.yaml` to configure:

- Replica count
- Resource limits
- Database connections
- Kafka settings
- Environment variables

## Verify Deployment

```bash
# Check pod status
kubectl get pods -n paklog -l app.kubernetes.io/name=order-management

# Check service
kubectl get svc -n paklog order-management

# View logs
kubectl logs -n paklog -l app.kubernetes.io/name=order-management -f

# Check health
kubectl port-forward -n paklog svc/order-management 8080:8080
curl http://localhost:8080/actuator/health
```

## Update Deployment

```bash
helm upgrade order-management . -n paklog
```

## Uninstall

```bash
helm uninstall order-management -n paklog
```

## Deploy as Part of Platform

To deploy all services together, use the umbrella chart:

```bash
cd ../../../../deployments/helm/paklog-platform
helm dependency update
helm install paklog-platform . -n paklog --create-namespace
```

See [Platform Documentation](../../../../deployments/helm/README.md) for more details.

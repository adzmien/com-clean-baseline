# ⚙️ Kubernetes Quickstart for Developers

A clean and practical collection of essential `kubectl` commands for day-to-day development.  
This guide is optimized for local Kubernetes clusters such as **Kind** and **Minikube**, and includes real-world examples, shortcuts, and reliable patterns for debugging workloads, connecting to databases, and managing Kubernetes resources efficiently.

---

## 🧭 Common CLI

<details>
  <summary>Click to expand</summary>

### 🔎 Get cluster info

```bash
kubectl cluster-info
```

![cluster-info](./images/kubectl/00-cluster-info.png "cluster-info")

---

### 🗂 List all namespaces

```bash
kubectl get ns
```

![ns](./images/kubectl/01-list-ns.png "list-namespaces")

---

### 📋 List all resources in the namespace

```bash
kubectl get all -n com-clean-dev
```

![resources](./images/kubectl/02-list-resources.png "list-resources")

---

### 📋 List by type in the namespace

```bash
kubectl get pods -n com-clean-dev
```

![resources](./images/kubectl/03-list-by-type.png "list-by-type")

</details>

---

</details>
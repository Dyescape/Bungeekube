# Bungeekube
Bungeecord plugin to support Kubernetes service discovery and configuration.

## How it works
The plugin utilises the Kubernetes API to search for applicable fallback servers that can be configured in the
Bungeecord server that this plugin is installed on. Discovery can be done either by watching pods or headless
services.

## Installation
Below are the installation steps necessary to set up this software.

### 1) Bungeecord
First, simply drag and drop the plugin Jar into the plugins folder of Bungeecord.

### 2) RBAC permissions
Now it's time to create a new role for the Bungeecord pod. These permissions are necessary as the plugin needs to use
the Kubernetes API to search for fallback pods or headless services. The below example can be used for reference.
Depending on what you configure in the plugin, either the pod or service permission can be removed. If you enable
headless service scanning, then pod scanning can be disabled and the permission can be removed here, and visa versa.
```yaml
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: minecraft-proxy
  namespace: minecraft
rules:
  - apiGroups:
      - ""
    resources:
      - pods
      - services
    verbs:
      - get
      - list
      - watch
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: minecraft-proxy
  namespace: minecraft
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: minecraft-proxy
subjects:
- kind: ServiceAccount
  name: minecraft-proxy
  namespace: minecraft
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: minecraft-proxy
  namespace: minecraft
```

### 3) Pod or service setup
We have two options in this step. Either we can annotate our Minecraft pods, or we can set up a headless service to help
us with the discovery. The difference is simply that the pod scanning setup will iterate over a set of pods and look for
any which have the required annotation and expected/configured ports open. The advantage is that this setup allows the
discovered fallback serves to be identified by name, but this approach will be slower on larger setups.

The second option is to use a headless service to assist this plugin in its discovery. When choosing this option, ensure
you understand what the difference between a [service](https://kubernetes.io/docs/concepts/services-networking/service/) 
and [headless service](https://kubernetes.io/docs/concepts/services-networking/service/#headless-services) is. The
advantage of using a headless service is that discovery is faster for larger networks, but fallback servers cannot be
identified by name as only IP addresses will be returned by this headless service.

Below is a simple table listing all possible annotations and their functionality.

| Annotation                                   | Description                                                        |
| ---------------------------------------------| ------------------------------------------------------------------ |
| `bungeekube.dyescape.com/enabled: 'true'`    | Base annotation to mark services or pods applicable for discovery. |

#### Annotated pod
Below we have an example of a StatefulSet which complies with this plugin. Note that only the template annotations are
important here. This serves purely as an example to showcase the annotation usage.
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: minecraft-server
  namespace: minecraft
spec:
  selector:
    matchLabels:
      component: minecraft-server
  serviceName: minecraft-server
  template:
    metadata:
      labels:
        component: minecraft-server
      annotations:
        bungeekube.dyescape.com: 'true' # <---
    spec:
      containers:
      - name: minecraft-software
        image: minecraft-server:1.16.2
        imagePullPolicy: Always
        ports:
        - containerPort: 25565
```

#### Annotated headless service
Below we have an example of a compliant headless service. What's important to note here is the annotation and the
`clusterIP` field. The annotation is used for identification in the plugin, and the `clusterIP: None` makes it a
headless service. The below example is purely for reference.
```yaml
apiVersion: v1
kind: Service
metadata:
  name: minecraft-server
  namespace: minecraft
  annotations:
    bungeekube.dyescape.com: 'true' # <---
spec:
  clusterIP: None # <---
  selector:
    component: minecraft-server
  ports:
    - protocol: TCP
      port: 25565
      targetPort: 25565 
```

## Usage
The plugin is designed to automatically find and register discovered fallback services into the running Bungeecord
instance. The configuration can be changed according to your needs and setup. See the below reference.
```yaml
discovery:
  
TODO
```

In the spirit of Minecraft backends more suitable on cloud, this plugin also supports environment variables. Refer to
the below overview for a complete list of available environment variables.

TODO
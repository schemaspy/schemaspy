variable "VERSION" {
  default = "local"
}

variable "REGISTRY" {
  default = "docker.io/schemaspy"
}

variable "GITHUB_REF_NAME" {
  default = "$GITHUB_REF_NAME"
}

variable "GIT_BRANCH" {
  default = "${GITHUB_REF_NAME}"
}

variable "GITHUB_SHA" {
  default = "$GITHUB_SHA"
}

variable "GIT_REVISION" {
  default = "${GITHUB_SHA}"
}

group "default" {
  targets = [
    "schemaspy"
  ]
}

target "drivers" {
  cache-from = ["type=gha,scope=dkr-drivers"]
  target = "drivers"
  output = ["type=cacheonly"]
  cache-to = ["type=gha,mode=min,scope=dkr-drivers"]
}

target "schemaspy" {
  cache-from = ["type=gha,scope=dkr-schemaspy"]
  contexts = {
    drivers = "target:drivers",
  }
  labels = {
    "GIT_BRANCH" = "${GIT_BRANCH}"
    "GIT_REVISION" = "${GIT_REVISION}"
  }
  platforms = ["linux/amd64", "linux/arm64", "linux/arm/v7"]
  cache-to = ["type=gha,mode=min,scope=dkr-schemaspy"]
}

target "pr" {
  inherits = ["schemaspy"]
    tags = [
      "${REGISTRY}/schemaspy:pr",
    ]
    platforms = ["linux/amd64"]
    output = ["type=docker"]
}

target "snapshot" {
  inherits = ["schemaspy"]
  tags = [
    "${REGISTRY}/schemaspy:snapshot",
  ]
}

target "release" {
  inherits = ["schemaspy"]
  context = "./target/checkout"
  tags = [
    "${REGISTRY}/schemaspy:${VERSION}",
    "${REGISTRY}/schemaspy:latest",
  ]
}

# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

### Security

- **Request-derived file paths are confined.** Values from a request that were used
  to build local file paths (`key` in `video-processing`; `size` in `compress` and
  `uploader`; `input`, `model` and `synset` in `image-recognition`; object keys in
  `compress`) can no longer write outside a scratch location. `video-processing`,
  `compress`, `uploader` and `image-recognition` now use private per-request temporary
  directories; `thumbnailer` writes its output to a private temporary file.
  Previously `video-processing` used the request `key` directly as the local
  destination path, which allowed an absolute path such as `/tmp/x` to be overwritten.
- **Bucket names are deploy-time settings only.** A request that includes
  `input_bucket` or `output_bucket` is now rejected. Buckets are read only from
  configuration, and a missing required bucket produces a clear error.
- **Application files in the JVM container images are no longer owned by the
  runtime user.** The `COPY --chown` options were removed from the `Dockerfile.jvm`
  files, so the application JARs are owned by root. This is a partial hardening:
  in the `ubi-minimal` based images the `/deployments` directory itself is still
  owned by the runtime user (UID 1001), which could still delete and replace the
  files in it, so the JARs are not fully protected. Not yet built into a running
  service or tested.
- Base images updated: `ubi9/openjdk-17:1.24` (was `ubi8/openjdk-17:1.14`), `ubi9/ubi-minimal:9.9` (was `ubi8/ubi-minimal:8.8`) and
  `ubuntu:22.04` (was the end-of-life `ubuntu:20.04`) for `video-processing`.
- A workflow step no longer interpolates the branch name into a shell command.
- Workflows now declare least-privilege `permissions`, and the CI actions were
  updated (`checkout@v7`, `setup-java@v6`, `docker/login-action@v4`, all on Node 24).
- Added a Security Considerations section to the README describing what the
  benchmarks trust and the deployer's responsibilities.

### Changed (breaking)

- **`input_bucket` and `output_bucket` can no longer be sent in a request.**
  Configure them instead with the environment variables listed in
  `benchmarks/UsingCloudObjectStorage.md` (for example
  `KNATIVEBENCH_VIDEO_PROCESSING_INPUT_BUCKET`) or in `application.properties`.
  `compress` and `uploader` gained the settings
  `knativebench.compress.*` and `knativebench.uploader.*`.
- The sample services in `samples/ksvc/` read bucket names from a ConfigMap named
  `cos-buckets`; create it before deploying (see the README).
- `compress` and `uploader` reject a `size` that contains anything other than letters,
  digits, `_` and `-`.

### Fixed

- The `image-recognition`, `thumbnailer` and `video-processing` native Dockerfiles
  referred to `${JAVA_OPTION}` instead of `${JAVA_OPTIONS}`, so
  `-Dquarkus.http.host=0.0.0.0` was never passed to the application.
- Documentation spelling and naming errors (`server_port`, `objectKey`,
  `Ce-Specversion`).

### Removed

- Unused `MavenWrapperDownloader.java` copies from `graph-bfs`, `graph-mst` and
  `graph-pagerank`, and the duplicate class name in `helloworld`
  (`Sleep` is now `HelloWorld`).

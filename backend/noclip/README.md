## noclip.website service

This is a customized version of the **noclip.website** frontend running on our
infrastructure.

The file server for providing game assets is the same as the one used by the
official public [frontend](https://noclip.website/). However, our version
includes the following key changes:

- [x] Disabled GUI 
- [x] Disabled file drag & drop functionality 
- [ ] Revamped camera controller 
- [ ] Restrain player movement to a small box around the initial position
- [ ] Removed the hash containing map+camera position from the URL

### Developping

Additional `Dockerfile.dev` and `docker-compose.dev.yml` are provided. They
mount the `noclip.website` directory contents and run a Node development server
instead of exposing the build result with Nginx.

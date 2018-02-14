Schema

- bit/author
- bit/namespace (optional?)
- bit/name
- bit/meta
  - bit.meta/doc
  - bit.meta/tag
- bit.meta/body-clj
- bit.meta/body-cljs
- bit.meta/spec
- bit.meta/examples
- bit.meta/created
- bit.meta/updated
- bit.meta/keywords

- user/id
- user/api-key
- user/name

- index/stem
- index/bits

Client

[ ] Cache

Server

[ ] Persistent storage

API

[v] /api/request-sign-in?email=...
[ ] /api/sign-in?token=...
[ ] /api/publish?fqn=...&body=...&body-cljs=...&doc=...
[ ] /api/update?fqn=...&body=...&body-cljs=...&doc=...
[ ] /api/delete?fqn=...
[v] /api/bits/<path>.edn
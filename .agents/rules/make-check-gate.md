## Make Check Gate

Run `make check` locally before declaring any task done, completed, addressed, or accomplished. 0 failures are required. Never leave verification for CI/CD or the user to discover.

**CRITICAL (Context Window Optimization):**
Do not run `make check` directly, as the massive stdout/stderr will saturate your context window. Instead, buffer and filter the output:
```bash
make check > build.log 2>&1 && echo "Check passed successfully" || (echo "Check failed. Last 100 lines:" && tail -n 100 build.log)
```

## Make Check Gate

Run `make check` locally before declaring any task done, completed, addressed, or accomplished. 0 failures are required. Never leave verification for CI/CD or the user to discover.

### Fast Iteration (`make fast-check`)
During active development and rapid iteration, run `make fast-check` for fast incremental feedback using the Gradle daemon and build cache (~15–30s):
```bash
make fast-check > build.log 2>&1 && echo "Fast check passed" || (echo "Fast check failed. Last 100 lines:" && tail -n 100 build.log)
```

### Final Validation (`make check`)
Before completing any task or PR, run `make check` to verify full cold-start CI parity (~1.5–2 min):
```bash
make check > build.log 2>&1 && echo "Check passed successfully" || (echo "Check failed. Last 100 lines:" && tail -n 100 build.log)
```

**CRITICAL (Context Window Optimization):**
Do not run `make check` or `make fast-check` directly without output redirection, as the massive stdout/stderr will saturate your context window. Always buffer and filter the output as shown above.

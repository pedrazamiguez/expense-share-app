# ADR-0008: Local Documentation RAG MCP Server for Agent Workflows

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-09-03
* **Tags:** ai, agents, rag, mcp, fastembed, sqlite, embeddings, architecture

---

## Context and Problem Statement

SplitTrip uses specialized AI coding agents grounded in code intelligence MCP tools (`codebase-memory-mcp`, `graphify`) to query syntax trees, call graphs, and architectural topologies. However, these tools are structurally oriented: they understand *code syntax and call hierarchies*, but have no semantic awareness of *business invariants, domain rationale, or architectural decisions* contained in `/docs/` (39 files, ~51k words across domain models, ADRs, design system tokens, and engineering guidelines) and `AGENTS.md`.

Currently, agents access project documentation through:
1. Static summaries in `AGENTS.md`.
2. Heuristic lazy-loading instructions in skills (e.g., "Read `docs/domain/add-ons-and-splits.md` if handling splits", introduced in #1201).

This creates two distinct failure modes:
* **Token Bloat:** Loading entire multi-thousand-word documentation files into context when only a single subsection or invariant was required.
* **Hallucination & Domain Drift:** Agents skipping manual file reads to conserve tokens and subsequently violating domain invariants (e.g., FIFO cash tranche handling, strict BigDecimal precision rules, or subunit share distributions).

How can we provide AI agents with sub-millisecond, semantic retrieval of authoritative documentation subsections while preserving 100% privacy, zero cloud leakage, and zero recurring API costs?

---

## Decision Drivers

* **Zero Cloud Leakage & Zero API Cost:** Embeddings and vector searches must execute 100% locally on CPU without external API keys (OpenAI, Gemini, Pinecone, Weaviate).
* **High Precision & Semantic Grounding:** Retrieval must capture domain invariants accurately, linking specific sections, breadcrumbs, and file paths.
* **Low Latency (<150ms):** Fast retrieval that does not block interactive agent turns or inflate tool overhead.
* **Seamless Tool Integration:** Exposure via a standard Model Context Protocol (MCP) server consumable across developer environments (OpenCode, Antigravity/Gemini).
* **Isolated Runtime:** Minimal developer friction, managed via `uv` PEP 723 without polluting global Python environments.

---

## Considered Options

1. **Full-File Lazy Loading (Status Quo):** Agents read whole markdown files via standard file view tools based on heuristic guidance in skills. Suffers from token bloat and frequent omission.
2. **Cloud Vector Database & Embedding APIs:** Storing documentation in Pinecone, Weaviate Cloud, or Qdrant Cloud using OpenAI/Gemini embedding endpoints. Rejected due to recurring costs, cloud dependency, network latency, and privacy leakage of internal documentation.
3. **Local Heavy Vector DB Daemon (Chroma/Qdrant standalone services):** Running local background database server processes. Rejected due to operational complexity, daemon management, port conflicts, and memory footprint.
4. **Embedded SQLite Hybrid RAG via Local CPU ONNX (`fastembed`) (Chosen):** Header-aware semantic chunking, local CPU ONNX embeddings (`BAAI/bge-small-en-v1.5`), in-memory normalized vector cosine similarity, and SQLite FTS5 BM25 search combined via Reciprocal Rank Fusion (RRF) exposed through a stdio FastMCP server.

---

## Decision Outcome

Chosen option: **Option 4 (Embedded SQLite Hybrid RAG via Local CPU ONNX)**.

### Architecture & Pipeline

```
Markdown Docs (/docs/**/*.md, AGENTS.md)
   │
   ▼
[Header-Aware Chunker] (MarkdownChunker)
   │ Tracks heading hierarchy (#, ##, ###) into breadcrumbs
   │ Preserves code fences intact; splits on paragraph boundaries
   │
   ▼
[Local Dense Embedder] (fastembed: BAAI/bge-small-en-v1.5, ONNX Runtime CPU)
   │ Generates 384-dimensional dense vectors
   │
   ▼
[Local SQLite Storage] (.cache/splittrip_rag.db)
   ├── documents (file_path, content_hash, updated_at)
   ├── chunks (chunk_id, file_path, breadcrumbs, content, embedding BLOB)
   └── chunks_fts (FTS5 full-text virtual table)
   │
   ▼
[FastMCP Stdio Server] (scripts/rag_mcp_server.py)
   ├── search_project_knowledge(query, top_k): Hybrid Dense + BM25 RRF Search
   ├── get_doc_section(file_path, section): Targeted section / document reader
   └── reindex_project_knowledge(force): Incremental re-indexer
```

### Key Technical Properties

1. **Header-Aware Chunking:**
   Headings are parsed into breadcrumb trails (e.g. `Domain > Cash Tranches and FIFO > The FIFO Settlement Invariant`). Content sections preserve code blocks intact. Sections exceeding 400 words are cleanly partitioned on paragraph boundaries with part suffixes.

2. **In-Memory Hybrid Search Engine:**
   On server initialization, normalized 384-d vectors are loaded into an in-memory matrix `(N, 384)`. Dense cosine similarity is computed in $<1\text{ms}$ via `np.dot`. Sparse BM25 retrieval runs via SQLite FTS5. Results are merged using Reciprocal Rank Fusion ($k=60$):
   $$RRF(d) = \sum_{m \in \{\text{dense}, \text{sparse}\}} \frac{1}{60 + \text{rank}_m(d)}$$

3. **Developer & Tooling Integration:**
   - Incremental indexing via `make rag-index` or `scripts/rag_indexer.py`.
   - Unified multi-index refresh target `make knowledge-update` (synchronizing `codebase-memory-mcp`, `splittrip-rag`, and `graphify`).
   - Setup and verification integrated into `make ai-setup` and `make doctor`.
   - Automatic registration in `~/.gemini/config/mcp_config.json` and `~/.config/opencode/opencode.jsonc`.

---

## Consequences

### Positive

* **Token Efficiency:** Agents retrieve precise 200–400 word chunks with breadcrumbs rather than 2,000–5,000 word full documents, saving tens of thousands of context tokens per issue.
* **100% Offline & Private:** Zero network requests for embedding or querying; runs entirely offline on CPU.
* **Blazing Fast:** Dense vector search takes $<1\text{ms}$; end-to-end hybrid retrieval takes $<25\text{ms}$.
* **Grounding Accuracy:** Queries for business invariants (e.g. FIFO cash rules, BigDecimal precision, subunit composition) immediately return the exact authoritative section.

### Negative / Trade-offs

* **Initial Embedding Time:** The first cold build takes ~60–90 seconds on CPU across the entire doc suite (~700 chunks). Incremental updates take $<0.1\text{s}$.
* **Disk Footprint:** The local SQLite index and ONNX model cache occupy ~2.3 MB for the database and ~130 MB for the cached embedding model in `~/.cache/fastembed/`.

---

## References

* [`docs/ai/ai-ecosystem.md`](../../ai/ai-ecosystem.md)
* [`docs/ai/code-intelligence-tools.md`](../../ai/code-intelligence-tools.md)
* [FastEmbed ONNX Runtime](https://github.com/qdrant/fastembed)
* [Model Context Protocol](https://modelcontextprotocol.io/)

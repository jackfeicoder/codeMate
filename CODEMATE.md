# CODEMATE.md

This file contains project-level context for codeMate. Future runtime versions may inject it into the system prompt when the CLI starts.

## Project Rules

- Keep the core flow clear: CLI -> Agent -> LLM -> Tool Registry -> Policy -> Renderer.
- Keep project naming, package naming, documents, and UI style independent.
- Keep runtime credentials in local environment files only.
- Keep generated files and build artifacts out of Git.
- Keep shipped behavior and documentation aligned.

## Current Objective

Deliver a production-oriented Java AI Agent CLI with a small, auditable core and room for optional extensions.

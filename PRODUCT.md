# Product

<!-- impeccable:product-schema 1 -->

## Platform

JavaFX desktop application.

## Users

Students ordering food at a self-service kiosk, kitchen staff preparing orders, and administrators managing the menu and reviewing activity.

## Product Purpose

Smart Canteen POS lets students identify themselves, pay from a wallet, and receive an order token while giving staff a shared view of order progress.

## Operating Context

The application runs on a local campus network, with kiosk, kitchen, and admin instances sharing a database. Barcode input is represented by a focused text field so a USB scanner can type into it.

## Capabilities and Constraints

Must support student scan, menu browsing, cart quantities from 1-10, balance validation, receipts, order statuses, availability toggles, staff login, and transaction history. The production design calls for JavaFX, JDBC, MySQL, prepared statements, and an atomic payment transaction. This first runnable slice uses seeded in-memory data while the persistence layer is added.

## Brand Commitments

Minimal corporate interface with clear operational hierarchy and large touch-friendly controls.

## Evidence on Hand

The source Product Requirements Document is in `Smart Canteen POS – Product Requirements Document.docx`.

## Product Principles

- Make the next action obvious.
- Never hide balance, total, or order status.
- Preserve the cart when payment cannot proceed.
- Treat kitchen operations as a live queue.

# Database Migrations (Liquibase)

## Philosophy

- All schema changes are expressed as Liquibase changesets and applied automatically on startup.
- No ad-hoc schema changes in environments. Version control is the source of truth.

## Location

- Master changelog: typically `src/main/resources/db/changelog/db.changelog-master.yml`
- Child changesets: `src/main/resources/db/changelog/changes/` (recommended)

## Authoring a Changeset

1. Create a new file under `db/changelog/changes/`, e.g., `2025-09-14T1900-add-driver-table.yml`.
2. Add a unique `id` and your `author`.
3. Write change operations (e.g., `createTable`, `addColumn`, `addForeignKeyConstraint`, `insert`).
4. Include the new file from the master changelog.

Example snippet:

```yaml
databaseChangeLog:
  - changeSet:
      id: 2025-09-14-add-index-on-vehicle
      author: your.name
      changes:
        - createIndex:
            tableName: vehicle
            indexName: idx_vehicle_vin
            columns:
              - column:
                  name: vin
```

## Promotion

- Test locally using H2 or a local PostgreSQL instance.
- Validate in a development database.
- Roll forward is preferred; provide rollback statements when feasible.

## Rollback

- Include `rollback:` blocks where possible.
- Alternatively, create a new changeset to reverse the change if needed.

## Tips

- Avoid destructive operations without backups.
- Use `preConditions` to guard changes that depend on prior state.
- Keep changesets small and focused.

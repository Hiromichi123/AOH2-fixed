# Verification and release status

The initial patch was verified using the locally installed game's Java 8 runtime:

- National population: 1.8 billion, signed-int boundary, 2.4 billion and 4 billion; occupied-province filtering.
- Diplomacy: reject non-finite writes; preserve healthy values; recover invalid saved values using reverse/neutral rules; skip eliminated-country IDs; stop relation-list growth.
- Economy: reproduce the high-population recovery penalty and investment cancellation at the minimum of 99; verify the corrected factor, normal low-population behavior and intentional negative-growth behavior.
- JVM verification of all 38 modified classes.
- Differential installer: exact reconstructed executable hash, idempotence, backup/restore and rejection of unsupported versions.

An affected local save was also tested in memory. No campaign or save data is included in this repository.

Still pending: full graphical startup, a long campaign in the running game, broader game-version compatibility and Workshop upload/subscription validation. The package is a release candidate, not a claim of full gameplay validation.

The patch preserves existing integer APIs for compatibility. Several legacy population statistics remain capped, as described in both READMEs. The first historical source of every non-finite diplomacy value is not proven; the patch guards reads/writes and known risky arithmetic sites, rather than claiming all possible origins have been identified.

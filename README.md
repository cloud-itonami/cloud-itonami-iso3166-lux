# cloud-itonami-iso3166-lux

**LUX**: Grand Duchy of Luxembourg.

- Portail des marchés publics (pmp.lu / marches.public.lu) -- public procurement
- Registre de Commerce et des Sociétés (RCS, operated by Luxembourg Business
  Registers -- LBR) + autorisation d'établissement (business license)

AGPL-3.0-or-later.

## Market-entry / statute catalogs

Governed public-sector market-entry compliance actor, same architecture
as `cloud-itonami-iso3166-and`/`-mco`/`-lie` (minus any country-specific
bridge those siblings carry):

- `src/marketentry/{facts,governor,phase,sim,operation,registry,store,
  marketentryllm}.cljc` -- the actor. `facts.cljc` cites the Ministre de
  la Mobilité et des Travaux publics (Loi du 8 avril 2018 sur les
  marchés publics + Règlement grand-ducal du 8 avril 2018, as amended
  29 mai 2024), the Registre de Commerce et des Sociétés (Loi modifiée
  du 19 décembre 2002, operated by Luxembourg Business Registers) and
  the autorisation d'établissement business-license regime (Loi
  modifiée du 2 septembre 2011). `governor.cljc`'s FLAGSHIP check
  independently recomputes whether an engagement satisfies ALL FIVE
  Loi du 2 septembre 2011 Art. 4 / Art. 5 conditions for autorisation
  d'établissement (qualification, permanent effective management, a
  real link to the company, no social/tax evasion, and a genuine fixed
  établissement that is NOT a mere domiciliation) -- a conjunctive,
  multi-condition legal-authorization gate, genuinely different in
  shape from every prior sibling's flagship check. A second,
  non-flagship check independently recomputes the current 79 000 EUR
  restricted-procedure-without-publication / procédure négociée
  ceiling (Art. 20 + RGD Art. 151, as amended 2024 -- the original 2018
  text alone would have understated this at 60 000 EUR).
- `src/statute/facts.cljc` -- general-law catalog: Loi du 10 août 1915
  (sociétés commerciales), Code du travail (Livre 1er, Titre II --
  contrat de travail) and Loi modifiée du 4 décembre 1967 (impôt sur le
  revenu). CSSF (financial-sector supervision) was investigated and
  found sector-scoped, not a general market-entry gate -- deliberately
  excluded, the same honest-exclusion discipline `cloud-itonami-iso3166-lie`
  applied to Liechtenstein's FMA.

Every citation is curl / headless-Chrome-DOM-dump-verified against an
official source (legilux.public.lu, guichet.public.lu, marches.public.lu,
cssf.lu); see each namespace's docstring for the full research trail and
any honestly-narrowed scope or disclosed gap (e.g. `rep-spec-basis` is
left nil for LUX -- not investigated this session, not force-fit).

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for Luxembourg:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.

## Development

```bash
clojure -M:dev:test
clojure -M:lint
clojure -M:dev:run   # drive the demo through one OperationActor
```

See CI (`.github/workflows/ci.yml`) for how the `kotoba-lang/langgraph` +
`kotoba-lang/langchain` + `kotoba-lang/langchain-store` sibling-checkout
layout is reconstructed on a bare single-repo checkout.

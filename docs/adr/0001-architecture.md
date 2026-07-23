# ADR-0001: LUX marketentry :implemented

Flagship `establishment-conditions-unmet` (Loi modifiée du 2 septembre
2011 réglementant l'accès aux professions d'artisan, de commerçant,
d'industriel ainsi qu'à certaines professions libérales, Arts. 3-5 --
five cumulative conditions: qualification+honorabilité professionnelle,
permanent effective management, a real link to the company, no
social/tax evasion, and a genuine fixed établissement that is NOT a
mere domiciliation under the loi modifiée du 31 mai 1999). Second,
non-flagship check `restricted-procedure-threshold-mismatch` (Loi du 8
avril 2018 sur les marchés publics, Art. 20 § 1er, alinéa 3, lettre a)
+ Règlement grand-ducal du 8 avril 2018, Art. 151, AS AMENDED by
règlement grand-ducal du 29 mai 2024 -- current ceiling 79 000 EUR,
replacing the original 2018 text's 60 000 EUR). `filing/submit` never
auto.

Grounded in directly-fetched primary sources (legilux.public.lu --
rendered via headless-Chrome DOM dump since it is a pure client-side
Angular SPA with zero server-side rendering, NOT a bot-detection wall
-- and guichet.public.lu, the Grand-Duchy's official one-stop
administrative portal): Loi du 8 avril 2018 sur les marchés publics
(ELI loi/2018/04/08/a243), Règlement grand-ducal du 8 avril 2018 (ELI
rgd/2018/04/08/a244) as amended by règlement grand-ducal du 29 mai 2024
(ELI rgd/2024/05/29/a226), Loi modifiée du 2 septembre 2011 (ELI
loi/2011/09/02/n1), Loi modifiée du 19 décembre 2002 concernant le
registre de commerce et des sociétés, Loi du 10 août 1915 concernant
les sociétés commerciales, Code du travail (Art. L. 121-1 - L. 121-4)
and Loi modifiée du 4 décembre 1967 concernant l'impôt sur le revenu.

CSSF (Commission de Surveillance du Secteur Financier) was investigated
and found sector-scoped (banking/investment/insurance/fund industry
under the loi modifiée du 5 avril 1993), not a general market-entry
gate, so it was deliberately excluded rather than force-fit -- the same
honest-exclusion discipline `cloud-itonami-iso3166-lie` applied to
Liechtenstein's FMA. `rep-spec-basis` is also honestly left nil for
LUX -- not investigated this session, not force-fit. See
`src/marketentry/facts.cljc` and `src/marketentry/governor.cljc` for
the full citation trail and gap disclosure.

(ns marketentry.facts
  "Per-jurisdiction public-procurement market-entry regulatory catalog
  -- the G2-style spec-basis table the Market-Entry Compliance Governor
  checks every `:jurisdiction/assess` proposal against ('did the advisor
  cite an OFFICIAL public source for this jurisdiction's requirements,
  or did it invent one?').

  Luxembourg's real market-entry surface (curl/headless-Chrome-DOM-dump
  verified 2026-07-23; legilux.public.lu -- the Grand-Duchy's official
  Journal officiel portal -- renders its actual law text ONLY via
  client-side JavaScript (a pure Angular SPA, confirmed by identical
  2381-byte shells across every `/eli/.../jo`, `/jo/fr/html`,
  `/jo/fr/pdf` and content-negotiated `Accept:` variant tried; no
  server-side rendering at all, unlike several other jurisdictions this
  loop has hit with plain server-rendered HTML). This is NOT a
  bot-detection wall (no CAPTCHA, no Cloudflare challenge, no
  anomaly page) -- it is an ordinary client-rendered public site, so it
  was read the same way a human researcher would: `google-chrome
  --headless=new --dump-dom` (isolated, invisible, no shared-desktop
  focus interaction) rendered the JS and the resulting DOM was
  pandoc-extracted to plain text and read directly. Every legilux
  citation below carries the rendered page's own confirmatory title
  line ('Journal officiel du Grand-Duché de Luxembourg' / the law's own
  first line), so none of this is a guess at what the SPA would have
  shown.

  - Procurement law was NOT assumed from the title alone. This
    iteration read Loi du 8 avril 2018 sur les marchés publics, Art. 20
    (curl-discovered ELI
    https://legilux.public.lu/eli/etat/leg/loi/2018/04/08/a243/jo,
    rendered and read directly, cross-linked from
    marches.public.lu/fr/legislation/marches-publics.html, the
    procurement portal's OWN legislation index) and its implementing
    Règlement grand-ducal du 8 avril 2018 (ELI a244), Art. 151. Art.
    20 Section 1 alinéa 3 lettre a) delegates the 'restricted procedure
    without publication / negotiated procedure' de-minimis ceiling to a
    règlement grand-ducal, capped in the STATUTE at '8 000 euros hors
    TVA, valeur cent de l'indice des prix à la consommation au 1er
    janvier 1948' (an index-linked 1948-base figure, NOT a flat euro
    amount -- Luxembourg's drafting convention for many decades-old
    thresholds). RGD Art. 151 supplies the CURRENT nominal figure. This
    iteration specifically checked whether that RGD figure was still
    current rather than assuming the first number found was authoritative,
    and found it was NOT: marches.public.lu's own legislation index lists
    a 'Règlement grand-ducal du 29 mai 2024 portant modification de
    l'article 151' -- fetched and read (ELI
    .../rgd/2024/05/29/a226/jo), Art. 1er: 'les termes « 60 000 euros »
    sont remplacés par ceux de « 79 000 euros ».' The CURRENT,
    in-force ceiling is therefore 79 000 EUR (HT), not the 60 000 EUR
    the original 2018 RGD text alone would suggest -- a concrete,
    verified illustration of why a citation must be checked against the
    latest amendment, not just the first hit. This vertical's flagship
    check does NOT ground on this threshold (see below); it is modeled
    as a second, non-flagship check (`restricted-procedure-*`
    accessors) precisely because AND's/other siblings' flagship checks
    already cover several numeric-threshold shapes, and Luxembourg's
    text yields a genuinely different, RICHER shape elsewhere (see the
    établissement finding).
  - Portal owner-authority: the Règlement ministériel du 18 janvier
    2021 instituant les conditions d'utilisation du portail des marchés
    publics (curl-discovered via pmp.lu's own 'gd.lu/pmp-conditions'
    link, ELI rmin/2021/01/18/a42, rendered and read directly) is
    issued by 'Le Ministre de la Mobilité et des Travaux publics' --
    the SAME ministry that signed the 2024 Art. 151 amendment (Yuriko
    Backes, Château de Berg, 29 mai 2024) -- so this catalog cites one
    consistent owner-authority for both the portal's usage terms and
    the procedure-threshold regulation, not two different guesses. The
    live portal itself was confirmed reachable directly: curl to
    https://pmp.lu redirects (HTTP 200) to pmp.b2g.etat.lu/entreprise,
    which names the operational contact info@pmp.public.lu / (+352)
    247-83377 and points to marches.public.lu for legislation.
  - Business/tax identity, and the ONE-ACT-VS-TWO-ACTS question this
    loop asks every iteration to investigate for its own country: this
    iteration found Luxembourg is a TWO-ACT, SEQUENTIALLY-DEPENDENT
    model -- closer in shape to Andorra's two-act model than to a
    single-guichet convergence, but with an explicit ORDERING
    Andorra's own catalog did not document for its two acts: the
    Registre de Commerce et des Sociétés (RCS) act and the autorisation
    d'établissement act are not just two separate authorities/statutes,
    they are SEQUENTIALLY DEPENDENT in one direction. guichet.public.lu
    -- the Luxembourg government's own one-stop administrative portal,
    curl-fetched and read directly (not a private secondary summarizer)
    -- states in its own live 'Demande d'autorisation d'établissement'
    page: 'L'octroi définitif de l'autorisation d'établissement requiert
    l'enregistrement des statuts au registre de commerce et des
    sociétés (RCS).' I.e. FINAL business-license issuance requires RCS
    registration to already exist -- the reverse ordering never holds.
    RCS itself is governed by the Loi modifiée du 19 décembre 2002
    concernant le registre de commerce et des sociétés ainsi que la
    comptabilité et les comptes annuels des entreprises (ELI
    loi/2002/12/19/n1, title confirmed via guichet.public.lu's own
    'Références légales' section on
    .../registre-commerce/depots-publications/immatriculation-entreprise-publication-rcs.html),
    operated by Luxembourg Business Registers (LBR, lbr.lu,
    curl-confirmed reachable). That same guichet.public.lu page
    (read directly, current) states the electronic-deposit deadline for
    privately-signed statuts ('au plus tard un mois après la signature
    des statuts') and that Recueil électronique des sociétés et
    associations (RESA) publication follows within 15 days of deposit,
    with third-party enforceability ('opposable aux tiers') running
    only from RESA publication -- reported honestly as a concrete,
    dated procedural fact, not a paraphrase of the statute's own text
    (which this iteration did not separately fetch article-by-article
    for these specific deadlines).
  - `rep-spec-basis`: for LUX this loop investigated whether Luxembourg's
    market-entry regime extends personal-exclusion grounds to a
    bidder's own representatives/directors -- the shape BGR's ЗОП Art.
    54(2)-(3), ALB's Neni 76(1), ARM's Article 6(1)(3) and AND's Art.
    13.1.a)/f) each document -- and did NOT re-derive this from Loi du
    8 avril 2018's own exclusion articles this session (that would
    require reading Arts. 29-31 of the procurement law, which this
    iteration did not fetch). Rather than force a citation this loop
    did not itself verify, `rep-spec-basis` is left nil for LUX,
    honestly, the same discipline Benin's and Antigua and Barbuda's
    catalogs already established for this accessor -- extend it in a
    future iteration with a directly-read citation, do not backfill a
    guess now.
  - The MPME/PIME-style bid-evaluation preference margin some siblings
    (Benin's Art. 77 al.3) ground their flagship check on: this
    iteration did not investigate whether Loi du 8 avril 2018 contains
    an analogous small/local-enterprise price-adjustment mechanism (a
    genuinely different question from the establishment-conditions and
    restricted-procedure-threshold questions this iteration DID answer)
    -- left uninvestigated rather than guessed at, honestly disclosed
    as a gap for a future iteration, not modeled as absent-with-
    confidence the way AND explicitly ruled it out for its own Art. 5.
  - This vertical's FLAGSHIP check does not ground on a threshold or a
    branching evidence-type gate (the shapes AND/MCO/LIE, and BGR/ALB/
    AZE/ARM/ATG/BEN before them, already cover) -- it grounds on Loi
    modifiée du 2 septembre 2011 réglementant l'accès aux professions
    d'artisan, de commerçant, d'industriel ainsi qu'à certaines
    professions libérales (the 'autorisation d'établissement'
    business-license law; ELI loi/2011/09/02/n1, cross-linked from
    guichet.public.lu's own 'Demande d'autorisation d'établissement'
    page, and both fetched/read directly this session), Art. 3
    ('L'autorisation d'établissement requise au préalable pour
    l'exercice d'une activité visée par la présente loi est délivrée
    par le ministre si les conditions d'établissement, d'honorabilité
    et de qualification prévues aux articles 4 à 27 sont remplies.'),
    Art. 4 (the designated dirigeant must CUMULATIVELY: 1. satisfy
    qualification+honorabilité professionnelle requirements; 2.
    effectively and permanently manage day-to-day operations; 3. have a
    genuine 'lien réel' with the company (owner/associate/shareholder/
    employee); 4. NOT have evaded social or tax charges, personally or
    through a company directed/formerly directed) and Art. 5 (the
    company must have a 'lieu d'exploitation fixe au Grand-Duché de
    Luxembourg' -- material installation, administrative/technical
    infrastructure, effective/permanent direction, the dirigeant's
    regular presence, on-site document retention -- and, in the SAME
    article's own final alinéa, an explicit carve-out this catalog
    treats as load-bearing: 'Une domiciliation au sens de la loi
    modifiée du 31 mai 1999 régissant la domiciliation des sociétés ne
    constitue pas un établissement au sens du présent article.' -- a
    mere registered-office domiciliation service does NOT count as an
    établissement). This is a GENUINELY DIFFERENT check SHAPE than
    every prior iso3166 sibling this repo mirrors: it is a CONJUNCTIVE
    (all-N-conditions-must-hold) legal-authorization gate over FIVE
    independently-sourced conditions plus one explicit statutory
    exclusion (a mere domiciliation is not an établissement), not a
    threshold, not a percentage, not a boolean registry-membership
    read, and not a branching which-evidence-applies gate.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  intake/portal-registration/filing evidence set; `:legal-basis` /
  `:owner-authority` / `:provenance` are the G2 citation the governor
  requires before any `:jurisdiction/assess` proposal can commit.
  `:corporate-number-*` grounds the RCS business-registration-number
  check; `:establishment-*` grounds this vertical's FLAGSHIP check
  (`establishment-conditions-spec-basis`); `:restricted-procedure-*`
  grounds a second, non-flagship, numeric-threshold check
  (`restricted-procedure-threshold-spec-basis`)."
  {"LUX" {:name "Grand Duchy of Luxembourg"
          :owner-authority "Ministre de la Mobilité et des Travaux publics (Grand-Duché de Luxembourg) -- issuer of both the portal's usage-conditions règlement ministériel (18 janvier 2021) and the Art. 151 procedure-threshold règlement grand-ducal (as amended 29 mai 2024)"
          :legal-basis "Loi du 8 avril 2018 sur les marchés publics + Règlement grand-ducal du 8 avril 2018 portant exécution de la loi du 8 avril 2018 sur les marchés publics, tel que modifié en dernier lieu par le règlement grand-ducal du 29 mai 2024 portant modification de l'article 151"
          :national-spec "Portail des marchés publics, live at pmp.lu (curl-confirmed HTTP 200, 2026-07-23, redirecting to pmp.b2g.etat.lu/entreprise; support info@pmp.public.lu / (+352) 247-83377); usage conditions instituted by règlement ministériel du 18 janvier 2021"
          :provenance "https://legilux.public.lu/eli/etat/leg/loi/2018/04/08/a243/jo"
          :required-evidence ["Registre de Commerce et des Sociétés (RCS) inscription record (Loi modifiée du 19 décembre 2002, operated by Luxembourg Business Registers -- LBR)"
                              "Autorisation d'établissement record (Loi modifiée du 2 septembre 2011, Art. 3 -- établissement, honorabilité, qualification conditions all satisfied; définitif issuance itself requires the RCS record above to already exist)"
                              "TVA (VAT) registration record (Administration de l'enregistrement, des domaines et de la TVA -- AED)"
                              "Authorized-representative confirmation record"]
          :corporate-number-owner-authority "Luxembourg Business Registers (LBR) -- operator of the Registre de Commerce et des Sociétés (RCS)"
          :corporate-number-legal-basis "Loi modifiée du 19 décembre 2002 concernant le registre de commerce et des sociétés ainsi que la comptabilité et les comptes annuels des entreprises -- electronic deposit of privately-signed statuts due 'au plus tard un mois après la signature des statuts', RESA publication within 15 days of deposit, third-party enforceability running only from RESA publication (guichet.public.lu, read directly, current)"
          :corporate-number-provenance "https://guichet.public.lu/fr/entreprises/gestion-juridique-comptabilite/registre-commerce/depots-publications/immatriculation-entreprise-publication-rcs.html"
          :rep-owner-authority nil
          :rep-legal-basis nil
          :rep-provenance nil
          :establishment-owner-authority "Ministre ayant les Classes moyennes dans ses attributions (Grand-Duché de Luxembourg) -- autorisation d'établissement is delivered 'par le ministre' per Art. 3 of the 2011 law"
          :establishment-legal-basis "Loi modifiée du 2 septembre 2011 réglementant l'accès aux professions d'artisan, de commerçant, d'industriel ainsi qu'à certaines professions libérales -- Art. 3 (master gate: établissement + honorabilité + qualification), Art. 4 (four cumulative dirigeant conditions: qualification+honorabilité, permanent effective management, real link to the company, no social/tax evasion), Art. 5 (fixed place of business in Luxembourg; a mere domiciliation under the loi modifiée du 31 mai 1999 does NOT constitute an établissement)"
          :establishment-provenance "http://legilux.public.lu/eli/etat/leg/loi/2011/09/02/n1/jo"
          :restricted-procedure-owner-authority "Ministre de la Mobilité et des Travaux publics -- pouvoirs adjudicateurs apply this ceiling directly when choosing procédure restreinte sans publication d'avis / procédure négociée over the ordinary open procedure"
          :restricted-procedure-legal-basis "Loi du 8 avril 2018 sur les marchés publics, Art. 20 § 1er, alinéa 3, lettre a) (statutory cap: 8 000 euros hors TVA, valeur cent de l'indice des prix à la consommation au 1er janvier 1948, delegated to règlement grand-ducal) + Règlement grand-ducal du 8 avril 2018, Art. 151, AS AMENDED by règlement grand-ducal du 29 mai 2024 Art. 1er (current nominal ceiling: 79 000 euros, replacing the original 2018 text's 60 000 euros)"
          :restricted-procedure-provenance "https://legilux.public.lu/eli/etat/leg/rgd/2024/05/29/a226/jo"}
   "USA" {:name "United States"
          :owner-authority "U.S. General Services Administration (GSA) / SAM.gov"
          :legal-basis "Federal Acquisition Regulation (FAR); System for Award Management"
          :national-spec "SAM.gov entity registration + NAICS self-certification"
          :provenance "https://sam.gov/"
          :required-evidence ["EIN record"
                              "SAM.gov registration record"
                              "State business registration record"
                              "Authorized-representative record"]}
   "DEU" {:name "Germany"
          :owner-authority "Beschaffungsamt des BMI / e-Vergabe platforms"
          :legal-basis "Gesetz gegen Wettbewerbsbeschränkungen (GWB) / VgV"
          :national-spec "e-Vergabe supplier registration under EU procurement directives"
          :provenance "https://www.evergabe-online.de/"
          :required-evidence ["Handelsregister extract"
                              "e-Vergabe registration record"
                              "USt-IdNr record"
                              "Authorized-representative record"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to assess or file
  on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-lux R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog for market-entry navigation, "
                 "not a survey of all ~194 jurisdictions -- extend "
                 "`marketentry.facts/catalog`, never fabricate a "
                 "jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))

(defn rep-spec-basis
  "The jurisdiction's representative-related requirement map, or nil when
  this catalog has no such regime, OR when this catalog has not yet
  investigated it. For LUX this is honestly nil -- see the `catalog`
  docstring's disclosure (not investigated this session, not
  force-fit)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:rep-owner-authority sb)
      (select-keys sb [:rep-owner-authority :rep-legal-basis :rep-provenance]))))

(defn corporate-number-spec-basis
  "The jurisdiction's corporate-number / business-registration regime, or nil."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:corporate-number-owner-authority sb)
      (select-keys sb [:corporate-number-owner-authority
                       :corporate-number-legal-basis
                       :corporate-number-provenance]))))

(defn establishment-conditions-spec-basis
  "The jurisdiction's autorisation d'établissement (business-license)
  conditions regime, or nil. For LUX this is real and current -- the
  FLAGSHIP check this vertical adds is grounded here (Loi modifiée du 2
  septembre 2011, Arts. 3-5)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:establishment-owner-authority sb)
      (select-keys sb [:establishment-owner-authority
                       :establishment-legal-basis
                       :establishment-provenance]))))

(defn restricted-procedure-threshold-spec-basis
  "The jurisdiction's restricted-procedure-without-publication /
  negotiated-procedure de-minimis threshold regime, or nil. For LUX
  this is real and current -- the 79 000 EUR ceiling as amended 29 mai
  2024 (see `catalog` docstring)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:restricted-procedure-owner-authority sb)
      (select-keys sb [:restricted-procedure-owner-authority
                       :restricted-procedure-legal-basis
                       :restricted-procedure-provenance]))))

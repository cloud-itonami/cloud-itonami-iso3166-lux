(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of Luxembourg procurement/business-license law, whether a
  claimed engagement fee actually equals base + months x rate, whether
  the engagement's own declared autorisation-d'établissement conditions
  actually reflect Loi du 2 septembre 2011 Arts. 3-5's own cumulative
  requirements, whether the engagement's own declared light-touch-
  procedure claim actually reflects Loi du 8 avril 2018 Art. 20 + RGD
  Art. 151 (as amended 2024)'s own 79 000 EUR ceiling, or when a draft
  stops being a draft and becomes a real-world pmp.lu (Portail des
  marchés publics) portal submission, so this MUST be a separate system
  able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints).

  This blueprint's own text (docs/business-model.md Trust Controls:
  'any actual portal registration or filing submission requires
  Market-Entry Compliance Governor clearance and always escalates to
  human sign-off'; 'a false or fabricated regulatory-requirement claim
  is a HARD hold') names exactly the checks below.

  Seven checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist on file?
    3. Établissement conditions
       unmet                        -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-etablissement-
                                       authorization? true`,
                                       INDEPENDENTLY recompute whether
                                       ALL FIVE Loi du 2 septembre 2011
                                       Art. 4 / Art. 5 conditions
                                       (qualification+honorabilité,
                                       permanent effective management,
                                       real link to the company, no
                                       social/tax evasion, a genuine
                                       fixed établissement that is NOT a
                                       mere domiciliation) hold, and
                                       HARD-hold if any is unmet.
                                       FLAGSHIP genuinely new check for
                                       the iso3166 family (grep-verified
                                       absent as a governor check
                                       function name fleet-wide at
                                       build time) -- a CONJUNCTIVE
                                       (all-N-conditions) legal-
                                       authorization gate, a shape
                                       distinct from AND's two-axis
                                       threshold, MCO's branching-
                                       which-evidence gate, Bulgaria's
                                       percentage-of-turnover formula,
                                       Albania's flat-constant
                                       threshold, Azerbaijan's/Armenia's
                                       boolean registry-membership
                                       reads, Antigua and Barbuda's
                                       single-axis three-tier
                                       classification, and Benin's
                                       bid-evaluation price adjustment.
    4. Restricted-procedure
       threshold mismatch          -- for `:filing/submit`,
                                       INDEPENDENTLY recompute Loi du 8
                                       avril 2018 Art. 20 + RGD Art. 151
                                       (as amended 29 mai 2024)'s own
                                       79 000 EUR de-minimis ceiling,
                                       and HARD-hold if the engagement's
                                       own declared `:light-touch-
                                       procedure-claim?` does not match.
                                       Second, non-flagship, numeric-
                                       threshold check -- unconditional
                                       (applies to every `:filing/
                                       submit`, the same 'always
                                       applies' shape Bulgaria's tax-
                                       arrears check and Azerbaijan's/
                                       Armenia's registry-membership
                                       checks use).
    5. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    6. Already drafted / already
       submitted                    -- double-actuation guards, enforced
                                       off dedicated `:drafted?`/
                                       `:submitted?` facts (never a
                                       `:status` value).
    7. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real portal package and submitting a real portal
  registration are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(RCS登記/autorisation d'établissement/TVA登録/代理人確認等)が充足していない状態での提案"}]))))

(defn- establishment-conditions-unmet-violations
  "For `:filing/submit`, when the engagement declares
  `:requires-etablissement-authorization? true`, INDEPENDENTLY recompute
  whether ALL FIVE Loi du 2 septembre 2011 Art. 4 / Art. 5 conditions
  hold, and HARD-hold if any is unmet -- the flagship genuinely new
  check this vertical adds."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (registry/establishment-conditions-unmet? e)
        [{:rule :establishment-conditions-unmet
          :detail (str subject " のautorisation d'établissement条件(Loi 2 septembre 2011 Art.4/Art.5)が未充足: "
                      (pr-str (registry/establishment-missing-conditions e)))}]))))

(defn- restricted-procedure-threshold-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute Luxembourg's Art. 20 +
  RGD Art. 151 (as amended 2024) restricted-procedure eligibility and
  HARD-hold if the engagement's own declared claim does not match."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (registry/restricted-procedure-claim-mismatches? e)
        [{:rule :restricted-procedure-threshold-mismatch
          :detail (str subject " の申告手続適格性(light-touch-procedure-claim?="
                      (:light-touch-procedure-claim? e)
                      ")が独立再計算値(Art.20+RGD Art.151 as amended 2024, value="
                      (:contract-value e) " -> "
                      (registry/restricted-procedure-eligible? e)
                      ")と一致しない")}]))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (establishment-conditions-unmet-violations request st)
                           (restricted-procedure-threshold-mismatch-violations request st)
                           (engagement-fee-mismatch-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})

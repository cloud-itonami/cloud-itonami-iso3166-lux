(ns culture.facts
  "Country-level regional-culture catalog for Luxembourg (LUX) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"LUX"
   [{:culture/id "lux.dish.judd-mat-gaardebounen"
     :culture/name "Judd mat Gaardebounen"
     :culture/country "LUX"
     :culture/kind :dish
     :culture/summary "Savory dish of smoked pork collar and broad beans; one of the most widely recognized national dishes of Luxembourg."
     :culture/url "https://en.wikipedia.org/wiki/Judd_mat_Gaardebounen"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.dish.bouneschlupp"
     :culture/name "Bouneschlupp"
     :culture/country "LUX"
     :culture/kind :dish
     :culture/summary "Traditional Luxembourgish green bean soup with potatoes, bacon and onions."
     :culture/url "https://en.wikipedia.org/wiki/Bouneschlupp"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.dish.gromperekichelcher"
     :culture/name "Gromperekichelcher"
     :culture/country "LUX"
     :culture/kind :dish
     :culture/summary "Luxembourgish potato fritters -- shallow-fried pancakes made from grated potatoes with binding ingredients such as egg or flour."
     :culture/url "https://en.wikipedia.org/wiki/Gromperekichelcher"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.beverage.cremant-de-luxembourg"
     :culture/name "Crémant de Luxembourg"
     :culture/country "LUX"
     :culture/kind :beverage
     :culture/summary "Sparkling wine from Luxembourg's Moselle valley made according to the traditional method (méthode traditionnelle) of sparkling wine production."
     :culture/url "https://en.wikipedia.org/wiki/Cr%C3%A9mant_de_Luxembourg"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.product.moselle-luxembourgeoise-wine"
     :culture/name "Moselle Luxembourgeoise wine"
     :culture/country "LUX"
     :culture/kind :product
     :culture/summary "Wine produced under the Appellation Contrôlée Moselle Luxembourgeoise designation, from 1,290 hectares of vines along Luxembourg's 42 km Moselle river border with Germany."
     :culture/url "https://en.wikipedia.org/wiki/Moselle_wine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.craft.peckvillercher"
     :culture/name "Péckvillercher pottery whistles"
     :culture/country "LUX"
     :culture/kind :craft
     :culture/summary "Hand-crafted earthenware bird whistles traditionally made by potters in the village of Nospelt and sold at Luxembourg's Easter Monday Emaischen folk market."
     :culture/url "https://en.wikipedia.org/wiki/Emaischen"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.festival.schueberfouer"
     :culture/name "Schueberfouer"
     :culture/country "LUX"
     :culture/kind :festival
     :culture/summary "Annual Luxembourg City funfair held on the Glacis square, founded in 1340, traditionally running for 20 days around Saint Bartholomew's Day in August-September."
     :culture/url "https://en.wikipedia.org/wiki/Schueberfouer"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lux.heritage.old-city-of-luxembourg"
     :culture/name "Old City of Luxembourg"
     :culture/country "LUX"
     :culture/kind :heritage
     :culture/summary "Historic quarters and fortifications of Luxembourg City, officially designated \"City of Luxembourg: its Old Quarters and Fortifications\" and inscribed as a UNESCO World Heritage Site in 1994."
     :culture/url "https://en.wikipedia.org/wiki/Old_City_of_Luxembourg"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-lux culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "LUX"))
                 " LUX entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))

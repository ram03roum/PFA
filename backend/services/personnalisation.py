# services/personalization.py
from services.data_collector import DataCollector
from services.algorithm_filter import AlgorithmFilter
from services.prompt_builder import PromptBuilder
from services.llm_service import LLMService
from services.cache_service import CacheService


class PersonalizationEngine:
    
    def __init__(self):
        self.collector      = DataCollector()
        self.algorithm      = AlgorithmFilter()
        self.prompt_builder = PromptBuilder()
        self.llm            = LLMService()
        self.cache          = CacheService()

    def get_recommendations(self, user_id, force_refresh=False):
        """
        Point d'entrée unique du moteur de personnalisation.

        Flux :
        1. Vérifier le cache
        2. Collecter les données MySQL
        3. Algorithme → 15 candidates
        4. LLM → Top 5 + explications
        5. Stocker en cache
        6. Retourner le résultat
        """
        # force_refresh = True  #testt 

        # ── 1. Vérifier le cache ──────────────────────────────────
        if not force_refresh:
            cached = self.cache.get(user_id)
            if cached:
                return {
                    "source": "cache",
                    "data":   cached
                }

        # ── 2. Collecter les données ──────────────────────────────
        user_data        = self.collector.get_user_data(user_id)
        all_destinations = self.collector.get_all_destinations(
            exclude_ids=user_data['reserved_ids']
        )

        print(f"DEBUG Engine: user={user_id} | interactions={user_data['total_interactions']} | alpha={user_data['alpha']} | beta={user_data['beta']}")

        # ── 3. Algorithme → 15 candidates ─────────────────────────
        candidates = self.algorithm.filter_candidates(
            user_data, all_destinations, top_n=30
        )

        print(f"DEBUG Engine: {len(candidates)} candidates trouvées")

        # ── 4. LLM → Top 5 + explications ────────────────────────
        prompt      = self.prompt_builder.build_ranking_prompt(user_data, candidates)
        llm_results = self.llm.get_recommendations(prompt, user_id)

        # ── 5. Fallback si LLM échoue ────────────────────────────
        if llm_results is None:
            print("DEBUG Engine: LLM échoué → fallback algorithme")
            final_results = self._format_algo_fallback(candidates[:10])
            source        = "algorithm_fallback"
        else:
            print(f"DEBUG Engine: LLM OK → {len(llm_results)} recommandations")
            final_results = self._merge_results(candidates, llm_results)
            source        = "hybrid"

        # ── 6. Stocker en cache ───────────────────────────────────
        self.cache.set(user_id, final_results)

        return {
            "source": source,
            "data":   final_results
        }

    def _merge_results(self, candidates, llm_results):
        """
        Fusionne les scores algorithme et LLM.
        Score final = 0.4 × algo + 0.6 × llm
        """
        candidates_dict = {
            c['destination'].id: c for c in candidates
        }

        merged = []
        for rec in llm_results:
            dest_id = rec.get('destination_id')

            if dest_id not in candidates_dict:
                continue

            dest       = candidates_dict[dest_id]['destination']
            algo_score = candidates_dict[dest_id]['algo_score']
            llm_score  = rec.get('llm_score', 0)

            # Si le LLM a retourné un score < 1 c'est probablement l'algo_score
            # On le corrige automatiquement
            if llm_score < 1:
                llm_score = llm_score * 10  # convertir 0.17 → 1.7
                llm_score = max(llm_score, 6.0)  # minimum 6.0
            final_score = round(0.4 * algo_score + 0.6 * (llm_score / 10), 4)
            merged.append({
                "destination_id": dest_id,
                "name":           dest.name,
                "country":        dest.country,
                "continent":      dest.continent,
                "type":           dest.type,
                "avgRating":      dest.avgRating,
                "avgCostUSD":     dest.avgCostUSD,
                "bestSeason":     dest.bestSeason,
                "photoURL":       dest.photoURL,
                "unescoSite":     dest.unescoSite,
                "algo_score":     round(algo_score, 4),
                "llm_score":      round(llm_score, 4),
                "final_score":    final_score,
                "explanation":    rec.get('explanation', ''),
                "rank":           rec.get('rank', 0)
            })

        # Trier par score final décroissant
        return sorted(merged, key=lambda x: x['final_score'], reverse=True)

    def _format_algo_fallback(self, candidates):
        """
        Format de fallback quand le LLM est indisponible.
        Retourne les 5 meilleures candidates de l'algorithme.
        """
        result = []
        for i, c in enumerate(candidates):
            dest = c['destination']
            result.append({
                "destination_id": dest.id,
                "name":           dest.name,
                "country":        dest.country,
                "continent":      dest.continent,
                "type":           dest.type,
                "avgRating":      dest.avgRating,
                "avgCostUSD":     dest.avgCostUSD,
                "bestSeason":     dest.bestSeason,
                "photoURL":       dest.photoURL,
                "unescoSite":     dest.unescoSite,
                "algo_score":     round(c['algo_score'], 4),
                "llm_score":      None,
                "final_score":    round(c['algo_score'], 4),
                "explanation":    "Recommande selon vos preferences.",
                "rank":           i + 1
            })
        return result
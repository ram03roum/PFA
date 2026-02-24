from models import Destination


class PromptBuilder:

    def build_ranking_prompt(self, user_data, candidates):

        # Historique reservations
        past_destinations = []
        for resa in user_data['reservations']:
            dest = Destination.query.get(resa.destination_id)
            if dest:
                past_destinations.append(f"{dest.name} ({dest.country})")

        # Historique favoris
        favorites_names = []
        for fav in user_data['favorites']:
            dest = Destination.query.get(fav.destination_id)
            if dest:
                favorites_names.append(f"{dest.name} ({dest.country})")

        # Formatage des candidates
        candidates_text = ""
        for i, cand in enumerate(candidates):
            dest = cand['destination']
            candidates_text += (
                f"\n{i+1}. ID:{dest.id} | {dest.name}"
                f"\n   Pays      : {dest.country} ({dest.continent})"
                f"\n   Type      : {dest.type}"
                f"\n   Cout/pers : {dest.avgCostUSD}$"
                f"\n   Saison    : {dest.bestSeason}"
                f"\n   Note      : {dest.avgRating}/5"
                f"\n   UNESCO    : {'Oui' if dest.unescoSite else 'Non'}"
                f"\n   Score algo: {cand['algo_score']}"
                f"\n"
            )

        # Profil budget
        budget_profile = self._detect_budget_profile(user_data)

        # Construction du prompt
        prompt = (
            "Tu es un expert en recommandation de voyages personnalises.\n"
            "Analyse le profil utilisateur et recommande les 5 meilleures destinations.\n\n"
            "PROFIL UTILISATEUR :\n"
            f"- Destinations reservees : {', '.join(past_destinations) if past_destinations else 'Aucune encore'}\n"
            f"- Destinations favorites : {', '.join(favorites_names) if favorites_names else 'Aucune encore'}\n"
            f"- Profil budget          : {budget_profile}\n"
            f"- Nombre interactions    : {user_data['total_interactions']}\n\n"
            "DESTINATIONS CANDIDATES :\n"
            f"{candidates_text}\n"
            "TA TACHE :\n"
            "1. Analyse le profil de cet utilisateur\n"
            "2. Selectionne les 10 destinations les plus adaptees\n"
            "3. Pour chacune genere une explication courte (2 phrases max) en francais\n\n"
            "REGLES :\n"
            "- Tu DOIS retourner EXACTEMENT 10 destinations, ni plus ni moins\n"  
            "- Tu DOIS retourner exactement 10 destinations, pas moins\n" 
            "- Reponds UNIQUEMENT en JSON valide\n"
            "- Pas de texte avant ou apres le JSON\n"
            "- L'explication doit etre personnalisee pour cet utilisateur\n\n"
            "FORMAT DE REPONSE OBLIGATOIRE :\n"
           "{\n"
          '  "recommendations": [\n'
           "    {\n"
            '      "destination_id": <id entier ex: 18>,\n'
            '      "rank": <entier de 1 a 10, classe toutes les 10 destinations>,\n'
            '      "llm_score": <decimal entre 6.0 et 10.0 selon pertinence>,\n'
            '      "explanation": "<2 phrases personnalisees en francais>"\n'
          "    }\n"
           "  ]\n"
           "}\n\n"
          "IMPORTANT POUR llm_score :\n"
          "- C'est TON score personnel entre 6.0 et 10.0\n"
          "- 10.0 = parfait pour cet utilisateur\n"
          "- 6.0 = acceptable mais pas ideal\n"
          "- NE PAS copier algo_score\n"
          "- NE PAS mettre de valeurs inferieures a 6.0\n"
        )

        return prompt

    def _detect_budget_profile(self, user_data):

        costs = []

        for resa in user_data['reservations']:
            dest = Destination.query.get(resa.destination_id)
            if dest and dest.avgCostUSD:
                costs.append(dest.avgCostUSD)

        for fav in user_data['favorites']:
            dest = Destination.query.get(fav.destination_id)
            if dest and dest.avgCostUSD:
                costs.append(dest.avgCostUSD)

        if not costs:
            return "Non determine"

        avg_cost = sum(costs) / len(costs)

        if avg_cost < 200:
            return "Budget economique (moins de 200$ par personne)"
        elif avg_cost < 1500:
            return "Budget moyen (200$ a 1500$ par personne)"
        else:
            return "Budget eleve (plus de 1500$ par personne)"
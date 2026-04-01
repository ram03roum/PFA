# services/llm_service.py
import os
import json
import time
from groq import Groq
from models import LlmLog
from extensions import db


class LLMService:

    def __init__(self):
        self.client = Groq(api_key=os.getenv("GROQ_API_KEY"))
        self.model  = "llama-3.3-70b-versatile"

    def get_recommendations(self, prompt, user_id):
        """
        Envoie le prompt a Groq et retourne les recommandations.
        Retourne None si le LLM echoue (fallback algorithme).
        """
        start_time = time.time()

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "Tu es un moteur de recommandation de voyages. "
                            "Tu reponds TOUJOURS en JSON valide uniquement. "
                            "Jamais de texte en dehors du JSON."
                        )
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.3,
                max_tokens=2000,
                response_format={"type": "json_object"}
            )

            response_time = time.time() - start_time
            content       = response.choices[0].message.content
            tokens_used   = response.usage.total_tokens

            # Parser la reponse JSON
            result = json.loads(content)

            # Log succes
            self._log(
                user_id=user_id,
                tokens_used=tokens_used,
                response_time=response_time,
                success=True
            )

            # print(f"DEBUG LLM succes | tokens: {tokens_used} | temps: {response_time:.2f}s")

            return result.get('recommendations', [])

        except json.JSONDecodeError as e:
            # print(f"DEBUG LLM JSON invalide: {e}")
            self._log(user_id=user_id, success=False)
            return None

        except Exception as e:
            # print(f"DEBUG LLM erreur: {e}")
            self._log(user_id=user_id, success=False)
            return None

    def _log(self, user_id, tokens_used=0, response_time=0.0, success=True):
        """Enregistre chaque appel LLM dans llm_logs."""
        try:
            log = LlmLog(
                user_id=int(user_id),
                tokens_used=tokens_used,
                response_time=response_time,
                success=success
            )
            db.session.add(log)
            db.session.commit()
        except Exception as e:
            print(f"DEBUG LLM log erreur: {e}")
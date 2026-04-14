# PFA — Plateforme de réservation et assistant voyage

## Aperçu

Projet fullstack composé de :
- un backend Python/Flask
- un frontend Angular 21
- une base de données MySQL
- des services IA pour le chat et les recommandations
- des envois d'e-mails automatiques de relance

Ce projet permet de gérer des destinations, des réservations, des favoris, un tableau de bord utilisateur, avec une assistance conversationnelle et des recommandations intelligentes.

## Architecture

### Frontend

Dossier principal : `frontend/`
- Framework : Angular 21
- Outils : `pnpm`, `ng serve`, `ng build`
- Styles : Bootstrap, Tailwind, animate.css
- Structure : pages, composants, services, guard, interceptors
- Fonctionnalités UX : navigation, pages destinations, page favoris, tableau de bord, chat, etc.

### Backend

Dossier principal : `backend/`
- Framework : Flask
- ORM : SQLAlchemy
- Migrations : Flask-Migrate / Alembic
- Authentification : JWT
- CORS : configuré pour `http://localhost:4200`
- Base : MySQL via PyMySQL
- Modules importants :
  - `app.py` : application Flask principale
  - `extensions.py` : initialisation des extensions (`db`, `migrate`, `jwt`, `mail`, `scheduler`)
  - `models.py` : définitions des entités principales
  - `routes/` : blueprints API
  - `services/` : IA, mail, cache, recommandations, prompt builder
  - `tasks/scheduled_jobs.py` : tâches planifiées

### Intelligence Artificielle

- `backend/services/ai_service.py` : chat assistant connecté à Google Gemini
- `backend/services/llm_service.py` : recommandations via Groq
- `backend/routes/chat.py` et `backend/routes/recommendations.py` exposent les endpoints correspondants

## Fonctionnalités principales

- Inscription et connexion utilisateur
- Gestion de destinations et réservations
- Favoris et historique utilisateur
- Tableaux de bord administrateur/utilisateur
- Recommandations personnalisées
- Chat assistant IA
- Relances par e-mail planifiées
- Système d’authentification sécurisé JWT

## Prérequis

- Node.js
- `pnpm`
- Python 3.11+ (ou compatible)
- MySQL / MariaDB
- Clé API Google Gemini
- Clé API Groq
- Serveur SMTP pour l’envoi d’e-mails

## Installation

### Backend

```powershell
cd backend
python -m venv venv
venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

### Frontend

```powershell
cd frontend
pnpm install
```

## Configuration

Créez un fichier `.env` à la racine du dépôt avec les variables suivantes :

```env
SECRET_KEY=une_chaine_secrete
DB_USER=utilisateur_mysql
DB_PASSWORD=mot_de_passe_mysql
DB_HOST=localhost
DB_NAME=nom_de_la_base
MAIL_USERNAME=votre_email@example.com
MAIL_PASSWORD=mot_de_passe_email
MAIL_SERVER=smtp.example.com
MAIL_PORT=587
MAIL_USE_TLS=True
GEMINI_API_KEY=votre_cle_gemini
GEMINI_MODEL=gemini-2.5-flash
GROQ_API_KEY=votre_cle_groq
```

> `backend/app.py` charge le `.env` à partir de la racine du repo.

## Démarrage

### Lancer le backend

```powershell
cd backend
venv\Scripts\Activate.ps1
python app.py
```

Le backend écoute par défaut sur `http://127.0.0.1:5000`.

### Lancer le frontend

```powershell
cd frontend
pnpm start
```

Le frontend est accessible sur `http://localhost:4200`.

## Base de données et migrations

Le projet utilise Flask-Migrate pour les évolutions de schéma.

```powershell
cd backend
$env:FLASK_APP = 'app.py'
flask db migrate -m "nouvelle migration"
flask db upgrade
```

## Structure des endpoints

Le backend expose plusieurs blueprints :

- `auth` : inscription, connexion
- `destinations` : gestion des destinations
- `reservations` : réservation clients
- `favorites` : gestion des favoris
- `recommendations` : recommandations IA
- `chat` : assistant conversationnel
- `dashboard` : données de suivi
- `users` : gestion des utilisateurs

## Notes importantes

- CORS est configuré pour accepter `http://localhost:4200`.
- Les clés API et mots de passe ne doivent pas être inclus dans le dépôt.
- `backend/app.py` démarre également un scheduler qui lance une tâche de relance quotidienne.

## Ressources utiles

- Frontend Angular : `frontend/package.json`
- Backend Python : `backend/requirements.txt`
- Configuration IA : `backend/services/ai_service.py`
- Recommandations : `backend/services/llm_service.py`
- Tâches planifiées : `backend/tasks/scheduled_jobs.py`

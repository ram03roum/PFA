const http = require('http');
const fs = require('fs');
const path = require('path');

// Configuration
const API_URL = 'http://localhost:8081/api/v1/products?size=1000';
const OUTPUT_FILE = path.join(__dirname, 'dataset_finetuning.jsonl');

function fetchData(url) {
    return new Promise((resolve, reject) => {
        http.get(url, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch (e) {
                    reject(e);
                }
            });
        }).on('error', reject);
    });
}

function createConversation(messages) {
    return JSON.stringify({ messages }) + '\n';
}

// Helper to pick random element from array
function pickRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

const SYSTEM_PROMPT = "Tu es l'assistant virtuel expert de 'Atmo Design' (anciennement Bacoge). Tu aides les clients à trouver des matériaux de construction et des meubles design. Tu es poli, professionnel, concis et tu as un ton commercial mais chaleureux.";

// Static Knowledge Base (FAQ)
const FAQ_DATA = [
    {
        questions: ["Quels sont vos délais de livraison ?", "Combien de temps pour recevoir ma commande ?", "La livraison est rapide ?"],
        answer: "Nous livrons généralement sous 3 à 5 jours ouvrés partout en France métropolitaine. Pour les commandes volumineuses (matériaux de construction), le transporteur vous contactera pour fixer un rendez-vous."
    },
    {
        questions: ["Acceptez-vous les retours ?", "Puis-je renvoyer un article ?", "Comment se passe le remboursement ?"],
        answer: "Oui, vous disposez de 14 jours après réception pour nous retourner un article s'il ne vous convient pas. Les frais de retour sont à votre charge, sauf en cas de défaut du produit."
    },
    {
        questions: ["Avez-vous un magasin physique ?", "Où êtes-vous situés ?", "Puis-je venir voir les produits ?"],
        answer: "Atmo Design est une boutique principalement en ligne. Notre siège est basé à Paris, mais nous n'avons pas de showroom ouvert au public pour le moment."
    },
    {
        questions: ["Comment contacter le service client ?", "J'ai un problème avec ma commande", "Je veux parler à quelqu'un"],
        answer: "Vous pouvez contacter notre service client via le formulaire de la page 'Contact' ou par email à support@atmo-design.fr. Nous répondons sous 24h ouvrées."
    },
    {
        questions: ["Faites-vous des devis pour les pros ?", "Je suis un professionnel, ai-je des tarifs ?", "Tarifs artisans"],
        answer: "Absolument ! Nous proposons des tarifs préférentiels pour les professionnels du bâtiment et les architectes. Contactez-nous via la rubrique 'Pro' pour ouvrir un compte professionnel."
    }
];

async function generateDataset() {
    console.log('Récupération des produits...');
    try {
        const data = await fetchData(API_URL);
        const products = data.products || [];
        
        console.log(`Traitement de ${products.length} produits...`);
        
        const stream = fs.createWriteStream(OUTPUT_FILE, { flags: 'w' });
        let count = 0;

        // 1. Generate FAQ conversations
        FAQ_DATA.forEach(faq => {
            faq.questions.forEach(q => {
                stream.write(createConversation([
                    { role: "system", content: SYSTEM_PROMPT },
                    { role: "user", content: q },
                    { role: "assistant", content: faq.answer }
                ]));
                count++;
            });
        });

        // 2. Generate Product conversations
        products.forEach(p => {
            const name = p.name;
            const desc = p.description || "Un produit de qualité de notre catalogue.";
            const price = p.price + (p.currency === 'EUR' ? '€' : p.currency || '€');
            const category = p.category || "Mobilier";
            const style = p.style || "Contemporain";
            const material = p.material || "Matériaux premium";
            const dimensions = p.dimensions || "Dimensions standards";
            const stock = p.stockQuantity > 0 ? "En stock" : "Actuellement indisponible";

            // --- Scenario A: General Inquiry ---
            const questionsInfo = [
                `Parle-moi de ${name}`,
                `Qu'est-ce que le ${name} ?`,
                `Pouvez-vous me décrire le ${name} ?`,
                `Je voudrais des infos sur ${name}`
            ];
            const answersInfo = [
                `${name} est une pièce superbe de notre collection ${category}. ${desc}. Son style ${style} s'intègrera parfaitement dans votre intérieur.`,
                `Le modèle ${name} est très apprécié. C'est un produit ${category} au style ${style}. ${desc}`,
                `Voici ce qu'il faut savoir sur ${name} : ${desc}. Il est fabriqué en ${material}.`
            ];
            
            // Generate 2 variations per product
            for(let i=0; i<2; i++) {
                stream.write(createConversation([
                    { role: "system", content: SYSTEM_PROMPT },
                    { role: "user", content: pickRandom(questionsInfo) },
                    { role: "assistant", content: pickRandom(answersInfo) }
                ]));
                count++;
            }

            // --- Scenario B: Price ---
            const questionsPrice = [
                `Combien coûte ${name} ?`,
                `Quel est le prix de ${name} ?`,
                `Est-ce que ${name} est cher ?`,
                `Donne-moi le tarif pour ${name}`
            ];
            stream.write(createConversation([
                { role: "system", content: SYSTEM_PROMPT },
                { role: "user", content: pickRandom(questionsPrice) },
                { role: "assistant", content: `Le ${name} est actuellement proposé au prix de ${price}. C'est un excellent rapport qualité-prix pour du ${material}.` }
            ]));
            count++;

            // --- Scenario C: Technical Details (Dimensions/Material) ---
            stream.write(createConversation([
                { role: "system", content: SYSTEM_PROMPT },
                { role: "user", content: `Quelles sont les dimensions de ${name} ?` },
                { role: "assistant", content: `Le ${name} a les dimensions suivantes : ${dimensions}.` }
            ]));
            count++;

            stream.write(createConversation([
                { role: "system", content: SYSTEM_PROMPT },
                { role: "user", content: `En quelle matière est fait ${name} ?` },
                { role: "assistant", content: `Ce produit est fabriqué en : ${material}. C'est un choix durable et esthétique.` }
            ]));
            count++;

            // --- Scenario D: Availability ---
            stream.write(createConversation([
                { role: "system", content: SYSTEM_PROMPT },
                { role: "user", content: `Est-ce que ${name} est disponible ?` },
                { role: "assistant", content: `Concernant la disponibilité : ${stock}. ${p.stockQuantity > 0 ? 'Commandez-le vite !' : 'Revenez plus tard.'}` }
            ]));
            count++;

            // --- Scenario E: Category Recommendation (Implicit) ---
            stream.write(createConversation([
                { role: "system", content: SYSTEM_PROMPT },
                { role: "user", content: `Je cherche un ${category} style ${style}.` },
                { role: "assistant", content: `Je vous recommande vivement notre ${name}. ${desc} Il correspond exactement à l'esprit ${style} que vous recherchez.` }
            ]));
            count++;
        });
        
        stream.end();
        console.log(`Dataset généré avec succès : ${OUTPUT_FILE}`);
        console.log(`Nombre total d'exemples de conversation : ${count}`);
        console.log("Ce fichier est beaucoup plus riche et prêt pour un fine-tuning performant.");
        
    } catch (e) {
        console.error("Erreur:", e.message);
    }
}

generateDataset();

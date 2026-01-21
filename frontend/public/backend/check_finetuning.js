const OpenAI = require('openai');

// API Key provided by user
const API_KEY = process.env.OPENAI_API_KEY || 'YOUR_OPENAI_API_KEY';

const openai = new OpenAI({
  apiKey: API_KEY,
});

async function checkStatus() {
  try {
    console.log('Fetching fine-tuning jobs...');
    
    // List the 10 most recent fine-tuning jobs
    const list = await openai.fineTuning.jobs.list({ limit: 5 });

    if (list.data.length === 0) {
        console.log('No fine-tuning jobs found.');
        return;
    }

    console.log('\n--- Latest Jobs Status ---');
    list.data.forEach(job => {
        const createdDate = new Date(job.created_at * 1000).toLocaleString();
        console.log(`ID: ${job.id}`);
        console.log(`Created: ${createdDate}`);
        console.log(`Status: ${job.status.toUpperCase()}`);
        if (job.fine_tuned_model) {
            console.log(`Model Name: ${job.fine_tuned_model}`);
        }
        if (job.error) {
            console.log(`Error: ${JSON.stringify(job.error)}`);
        }
        console.log('--------------------------');
    });

  } catch (error) {
    console.error('Error fetching status:', error.message);
  }
}

checkStatus();

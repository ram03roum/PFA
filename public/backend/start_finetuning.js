const fs = require('fs');
const path = require('path');
const OpenAI = require('openai');

// API Key provided by user
const API_KEY = process.env.OPENAI_API_KEY || 'YOUR_OPENAI_API_KEY';

const openai = new OpenAI({
  apiKey: API_KEY,
});

const DATASET_PATH = path.join(__dirname, 'dataset_finetuning.jsonl');

async function main() {
  try {
    console.log('1. Uploading file to OpenAI...');
    
    if (!fs.existsSync(DATASET_PATH)) {
        throw new Error(`File not found: ${DATASET_PATH}`);
    }

    const file = await openai.files.create({
      file: fs.createReadStream(DATASET_PATH),
      purpose: 'fine-tune',
    });

    console.log(`   -> File ID: ${file.id}`);
    
    // Wait a bit for file processing
    console.log('2. Waiting 5 seconds for file processing...');
    await new Promise(resolve => setTimeout(resolve, 5000));

    console.log('3. Starting Fine-tuning job...');
    try {
        const job = await openai.fineTuning.jobs.create({
          training_file: file.id,
          model: 'gpt-3.5-turbo',
          hyperparameters: {
            n_epochs: 3
          }
        });
        console.log(`   -> Job started successfully!`);
        console.log(`   -> Job ID: ${job.id}`);
        console.log(`   -> Status: ${job.status}`);
    } catch (createError) {
        console.error('   -> FAILED to create job:', createError.message);
        if (createError.response) {
            console.error('   -> Response data:', JSON.stringify(createError.response.data));
        }
    }

  } catch (error) {
    console.error('Global Error:', error);
  }
}

main();

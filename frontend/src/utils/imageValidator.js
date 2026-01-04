import * as mobilenet from '@tensorflow-models/mobilenet';
import '@tensorflow/tfjs';

let model = null;

export const loadModel = async () => {
  if (!model) {
    console.log('Loading AI Model...');
    model = await mobilenet.load();
  }
  return model;
};

export const validateWatchImage = async (file) => {
  if (!file) return { isValid: false, error: 'No file provided' };

  if (file.size > 5 * 1024 * 1024) {
    return { isValid: false, error: 'Image must be less than 5MB' };
  }

  const loadedModel = await loadModel();

  return new Promise((resolve) => {
    const imgUrl = URL.createObjectURL(file);
    const img = new Image();
    img.src = imgUrl;
    img.crossOrigin = "anonymous";

    img.onload = async () => {
      try {
        const predictions = await loadedModel.classify(img);
        const isWatch = predictions.some(p =>
          ['watch', 'clock', 'timepiece', 'stopwatch'].some(keyword => p.className.toLowerCase().includes(keyword))
        );

        if (isWatch) {
          resolve({ isValid: true, previewUrl: imgUrl });
        } else {
          resolve({
            isValid: false,
            error: `This doesn't look like a watch. (AI detected: ${predictions[0].className})`
          });
        }
      } catch (err) {
        resolve({ isValid: false, error: 'Failed to classify image' });
      }
    };

    img.onerror = () => {
      resolve({ isValid: false, error: 'Failed to load image' });
    };
  });
};

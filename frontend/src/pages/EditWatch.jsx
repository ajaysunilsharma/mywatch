import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getWatch, updateWatch } from '../utils/api';
import * as mobilenet from '@tensorflow-models/mobilenet';
import '@tensorflow/tfjs';
import './AddWatch.css'; // Re-using styles

function EditWatch() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    brand: '',
    model: '',
    referenceNumber: '',
    year: '',
    price: '',
    description: ''
  });
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [model, setModel] = useState(null);
  const [isValidating, setIsValidating] = useState(false);

  // Load AI Model
  useEffect(() => {
    async function loadModel() {
      const loadedModel = await mobilenet.load();
      setModel(loadedModel);
    }
    loadModel();
  }, []);

  // Fetch Watch Data
  useEffect(() => {
    async function fetchWatch() {
      try {
        const response = await getWatch(id);
        const data = response.data.watch;
        setFormData({
          brand: data.brand || '',
          model: data.model || '',
          referenceNumber: data.referenceNumber || '',
          year: data.year || '',
          price: data.price || '',
          description: data.description || ''
        });
        setPreview(data.thumbnailUrl); // Show existing image
      } catch (err) {
        setError('Failed to load watch details');
      } finally {
        setLoading(false);
      }
    }
    fetchWatch();
  }, [id]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.id]: e.target.value
    });
  };

  const handleImageChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('Image must be less than 5MB');
        return;
      }

      // AI Validation for new image
      if (!model) {
        setImage(file);
        setPreview(URL.createObjectURL(file));
        return;
      }

      setIsValidating(true);
      setError('Verifying new image with AI...');

      const imgUrl = URL.createObjectURL(file);
      const img = new Image();
      img.src = imgUrl;
      img.crossOrigin = "anonymous";

      img.onload = async () => {
        try {
          const predictions = await model.classify(img);
          const isWatch = predictions.some(p => 
            ['watch', 'clock', 'timepiece', 'stopwatch'].some(keyword => p.className.toLowerCase().includes(keyword))
          );

          if (isWatch) {
            setImage(file);
            setPreview(imgUrl);
            setError('');
          } else {
            setError(`This doesn't look like a watch. (AI detected: ${predictions[0].className})`);
            setImage(null);
          }
        } finally {
          setIsValidating(false);
        }
      };
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    const data = new FormData();
    Object.keys(formData).forEach(key => {
      data.append(key, formData[key]);
    });
    if (image) {
      data.append('image', image);
    }

    try {
      await updateWatch(id, data);
      navigate(`/watch/${id}`); // Redirect to details page
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update watch');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading-page">Loading...</div>;

  return (
    <div className="add-watch">
      <div className="container">
        <div className="add-watch-header">
          <h1>Edit Watch</h1>
          <p>Update the details of this timepiece</p>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="add-watch-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="brand">Brand</label>
              <input
                type="text"
                id="brand"
                value={formData.brand}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="model">Model</label>
              <input
                type="text"
                id="model"
                value={formData.model}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="referenceNumber">Reference Number</label>
            <input
              type="text"
              id="referenceNumber"
              value={formData.referenceNumber}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="year">Year</label>
              <input
                type="number"
                id="year"
                value={formData.year}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="price">Price</label>
              <input
                type="text"
                id="price"
                value={formData.price}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              value={formData.description}
              onChange={handleChange}
              required
              rows="4"
            />
          </div>

          <div className="form-group">
            <label htmlFor="image">
              Update Image (Optional)
              {!model && <span style={{ marginLeft: '8px', fontSize: '0.85em', color: '#666' }}>(Initializing AI...)</span>}
            </label>
            <input
              type="file"
              id="image"
              accept="image/jpeg,image/jpg,image/png"
              onChange={handleImageChange}
              disabled={!model || isValidating}
            />
            {preview && (
              <div className="image-preview">
                <img src={preview} alt="Preview" />
              </div>
            )}
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn-outline"
              onClick={() => navigate(`/watch/${id}`)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={submitting || isValidating}
            >
              {submitting ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditWatch;

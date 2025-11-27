const form = document.getElementById('search-form');
const input = document.getElementById('pokemon-name');
const statusEl = document.getElementById('status');
const placeholder = document.getElementById('results-placeholder');
const card = document.getElementById('pokemon-card');

const fields = {
  id: document.getElementById('pokemon-id'),
  name: document.getElementById('pokemon-name-display'),
  types: document.getElementById('pokemon-types'),
  image: document.getElementById('pokemon-image'),
  height: document.getElementById('pokemon-height'),
  weight: document.getElementById('pokemon-weight'),
  exp: document.getElementById('pokemon-exp'),
  order: document.getElementById('pokemon-order'),
  abilities: document.getElementById('pokemon-abilities'),
  stats: document.getElementById('pokemon-stats'),
  moves: document.getElementById('pokemon-moves'),
  items: document.getElementById('pokemon-items'),
};

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const query = input.value.trim();
  if (!query) return;

  setStatus(`Searching for ${query}...`, false);
  toggleLoading(true);

  try {
    const response = await fetch(`/api/pokemon?name=${encodeURIComponent(query)}`);
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Unable to fetch Pokémon');
    }
    const data = await response.json();
    renderPokemon(data);
    setStatus(`Served in ${response.headers.get('x-response-time') || 'cached speed'} ⚡`, true);
  } catch (error) {
    console.error(error);
    setStatus(error.message, false);
  } finally {
    toggleLoading(false);
  }
});

function setStatus(message, success) {
  statusEl.textContent = message;
  statusEl.style.color = success ? '#34d399' : '#ff7a18';
}

function toggleLoading(isLoading) {
  form.querySelector('button').disabled = isLoading;
  form.querySelector('button').textContent = isLoading ? 'Searching...' : 'Search';
}

function renderPokemon(data) {
  placeholder.style.display = 'none';
  card.classList.remove('hidden');

  fields.id.textContent = `#${String(data.id).padStart(4, '0')}`;
  fields.name.textContent = capitalize(data.name);
  fields.types.textContent = data.types.map(capitalize).join(' • ');
  fields.image.src = data.sprite || 'https://placehold.co/200x200?text=No+Image';
  fields.image.alt = data.name;
  fields.height.textContent = `${(data.height / 10).toFixed(1)} m`;
  fields.weight.textContent = `${(data.weight / 10).toFixed(1)} kg`;
  fields.exp.textContent = data.baseExperience;
  fields.order.textContent = data.order;

  renderPills(fields.abilities, data.abilities.map(formatAbility));
  renderPills(fields.moves, data.moves);
  renderPills(fields.items, data.heldItems.length ? data.heldItems : ['None']);
  renderStats(fields.stats, data.stats);
}

function renderPills(container, items) {
  container.innerHTML = items.map(item => `<li>${capitalize(item)}</li>`).join('');
}

function renderStats(container, stats) {
  container.innerHTML = stats
    .map(stat => `
      <div class="stat-bar">
        <span>${stat.name}</span>
        <div class="bar">
          <span class="bar-fill" style="width: ${Math.min(stat.value, 180) / 1.8}%"></span>
        </div>
        <strong>${stat.value}</strong>
      </div>
    `).join('');
}

function formatAbility(ability) {
  return ability.hidden ? `${ability.name} (hidden)` : ability.name;
}

function capitalize(value) {
  if (typeof value !== 'string') return value;
  return value.charAt(0).toUpperCase() + value.slice(1);
}


import React, {useState, useEffect, useMemo} from 'react';
import {Client} from '@stomp/stompjs';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081';
const WS_URL = API_BASE.replace(/^http/, 'ws') + '/ws';

const CLIMATES = ['HUMIDSUBTROPICAL', 'MEDITERRANIAN', 'SUBARCTIC', 'TUNDRA'];
const GOVERNMENTS = ['IDEOCRACY', 'OLIGARCHY', 'STRATOCRACY', 'THALASSOCRACY'];

export default function App() {
    const [token, setToken] = useState(localStorage.getItem('token') || '');
    const [username, setUsername] = useState(localStorage.getItem('username') || '');
    const [activeTab, setActiveTab] = useState('cities');

    const [authMode, setAuthMode] = useState('login');
    const [authForm, setAuthForm] = useState({username: '', password: ''});

    const [cities, setCities] = useState([]);
    const [humans, setHumans] = useState([]);
    const [coordinates, setCoordinates] = useState([]);
    const [carCodes, setCarCodes] = useState([]);

    const [error, setError] = useState('');
    const [warning, setWarning] = useState('');
    const [success, setSuccess] = useState('');

    const [filters, setFilters] = useState({name: '', climate: '', government: '', governor: ''});
    const [sortConfig, setSortConfig] = useState({key: 'id', direction: 'asc'});
    const [page, setPage] = useState(1);
    const pageSize = 5;

    const [cityModalOpen, setCityModalOpen] = useState(false);
    const [editingCityId, setEditingCityId] = useState(null);
    const [viewCity, setViewCity] = useState(null);
    const [searchIdInput, setSearchIdInput] = useState('');

    const initialCityForm = {
        name: '', area: '', population: '', establishmentDate: '', capital: false,
        metersAboveSeaLevel: '', climate: 'HUMIDSUBTROPICAL', government: '',
        coordMode: 'existing', coordinatesId: '', newX: '', newY: '',
        govMode: 'existing', governorId: '', newGovName: '', newGovSurname: '', newGovAge: '',
        selectedCarCodes: [], // Массив для выбранных существующих кодов
        newCarCodesText: '' // Текст для новых кодов
    };
    const [cityForm, setCityForm] = useState(initialCityForm);

    const [humanForm, setHumanForm] = useState({id: null, name: '', surname: '', age: ''});
    const [coordForm, setCoordForm] = useState({id: null, x: '', y: ''});
    const [codeForm, setCodeForm] = useState({id: ''});

    const [thresholdInput, setThresholdInput] = useState('0');
    const [relocateCityId, setRelocateCityId] = useState('');
    const [specialResult, setSpecialResult] = useState('');

    const clearAlerts = () => {
        setError('');
        setWarning('');
        setSuccess('');
    };

    const apiFetch = async (path, options = {}) => {
        const headers = {'Content-Type': 'application/json', ...(options.headers || {})};
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const res = await fetch(`${API_BASE}${path}`, {...options, headers});
        if (res.status === 401) {
            handleLogout();
            throw new Error('Сессия истекла, войдите снова');
        }
        if (res.status === 204) return null;
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.message || `Ошибка ${res.status}`);
        return data;
    };

    const loadAllData = async () => {
        if (!token) return;
        try {
            const [c, h, coord, codes] = await Promise.all([
                apiFetch('/cities'),
                apiFetch('/humans'),
                apiFetch('/coordinates'),
                apiFetch('/car-codes')
            ]);
            setCities(c);
            setHumans(h);
            setCoordinates(coord);
            setCarCodes(codes);
        } catch (e) {
            setError(e.message);
        }
    };

    useEffect(() => {
        if (!token) return;
        loadAllData();

        const client = new Client({
            brokerURL: WS_URL,
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe('/topic/cities', () => {
                    loadAllData();
                });
            }
        });
        client.activate();
        return () => client.deactivate();
    }, [token]);

    const handleAuth = async (e) => {
        e.preventDefault();
        clearAlerts();
        try {
            const data = await apiFetch(`/auth/${authMode}`, {
                method: 'POST',
                body: JSON.stringify(authForm)
            });
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            setToken(data.token);
            setUsername(data.username);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setToken('');
        setUsername('');
    };

    const filteredAndSortedCities = useMemo(() => {
        let list = cities.filter(c => {
            const govStr = c.governor ? `${c.governor.name} ${c.governor.surname}` : '';
            return (
                (c.name || '').toLowerCase().includes(filters.name.toLowerCase()) &&
                (c.climate || '').toLowerCase().includes(filters.climate.toLowerCase()) &&
                (c.government || '').toLowerCase().includes(filters.government.toLowerCase()) &&
                govStr.toLowerCase().includes(filters.governor.toLowerCase())
            );
        });

        list.sort((a, b) => {
            let valA = a[sortConfig.key];
            let valB = b[sortConfig.key];
            if (sortConfig.key === 'governor') {
                valA = a.governor ? `${a.governor.name} ${a.governor.surname}` : '';
                valB = b.governor ? `${b.governor.name} ${b.governor.surname}` : '';
            }
            if (valA == null) return 1;
            if (valB == null) return -1;
            if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
            if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
            return 0;
        });
        return list;
    }, [cities, filters, sortConfig]);

    const totalPages = Math.max(1, Math.ceil(filteredAndSortedCities.length / pageSize));
    const paginatedCities = filteredAndSortedCities.slice((page - 1) * pageSize, page * pageSize);

    const toggleSort = (key) => {
        setSortConfig(prev => ({
            key,
            direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
        }));
    };

    const openCreateCityModal = () => {
        clearAlerts();
        setEditingCityId(null);
        setCityForm({
            ...initialCityForm,
            coordinatesId: coordinates[0]?.id || '',
            governorId: humans[0]?.id || '',
            coordMode: coordinates.length > 0 ? 'existing' : 'new',
            govMode: humans.length > 0 ? 'existing' : 'new',
            selectedCarCodes: []
        });
        setCityModalOpen(true);
    };

    const openEditCityModal = (city) => {
        clearAlerts();
        setEditingCityId(city.id);
        setCityForm({
            name: city.name,
            area: city.area,
            population: city.population,
            establishmentDate: city.establishmentDate ? city.establishmentDate.slice(0, 16) : '',
            capital: Boolean(city.capital),
            metersAboveSeaLevel: city.metersAboveSeaLevel ?? '',
            climate: city.climate,
            government: city.government || '',
            coordMode: 'existing',
            coordinatesId: city.coordinatesId || '',
            newX: '', newY: '',
            govMode: 'existing',
            governorId: city.governorId || '',
            newGovName: '', newGovSurname: '', newGovAge: '',
            selectedCarCodes: city.carCodeIds || [],
            newCarCodesText: ''
        });
        setCityModalOpen(true);
    };

    const toggleCarCode = (id) => {
        setCityForm(prev => {
            const isSelected = prev.selectedCarCodes.includes(id);
            return {
                ...prev,
                selectedCarCodes: isSelected
                    ? prev.selectedCarCodes.filter(c => c !== id)
                    : [...prev.selectedCarCodes, id]
            };
        });
    };

    const handleSaveCity = async (e) => {
        e.preventDefault();
        clearAlerts();
        try {
            // Собираем коды (и выбранные галочками, и новые из текстового поля)
            const newCodesArray = cityForm.newCarCodesText
                .split(',')
                .map(s => s.trim())
                .filter(Boolean)
                .map(Number);

            const allCarCodeIds = [...new Set([...cityForm.selectedCarCodes, ...newCodesArray])];

            const payload = {
                name: cityForm.name,
                area: Number(cityForm.area),
                population: Number(cityForm.population),
                establishmentDate: cityForm.establishmentDate ? new Date(cityForm.establishmentDate).toISOString() : null,
                capital: Boolean(cityForm.capital),
                metersAboveSeaLevel: cityForm.metersAboveSeaLevel !== '' ? Number(cityForm.metersAboveSeaLevel) : null,
                climate: cityForm.climate,
                government: cityForm.government || null,
                carCodeIds: allCarCodeIds
            };

            if (cityForm.coordMode === 'existing') {
                payload.coordinatesId = Number(cityForm.coordinatesId);
            } else {
                payload.coordinates = {x: Number(cityForm.newX), y: Number(cityForm.newY)};
            }

            if (cityForm.govMode === 'existing') {
                payload.governorId = Number(cityForm.governorId);
            } else {
                payload.governor = {
                    name: cityForm.newGovName,
                    surname: cityForm.newGovSurname,
                    age: Number(cityForm.newGovAge)
                };
            }

            const saved = await apiFetch(editingCityId ? `/cities/${editingCityId}` : '/cities', {
                method: editingCityId ? 'PUT' : 'POST',
                body: JSON.stringify(payload)
            });

            if (saved.warningMessage) {
                setWarning(saved.warningMessage);
            } else {
                setSuccess(editingCityId ? 'Город обновлен' : 'Город создан');
            }
            setCityModalOpen(false);
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDeleteCity = async (id) => {
        clearAlerts();
        try {
            await apiFetch(`/cities/${id}`, {method: 'DELETE'});
            setSuccess(`Город ID ${id} удален`);
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleSearchById = async (e) => {
        e.preventDefault();
        clearAlerts();
        try {
            const city = await apiFetch(`/cities/${searchIdInput}`);
            setViewCity(city);
        } catch (err) {
            setError(err.message);
        }
    };

    if (!token) {
        return (
            <div className="container" style={{maxWidth: 420, marginTop: 80}}>
                <div className="card">
                    <h2 style={{marginTop: 0}}>{authMode === 'login' ? 'Вход в систему' : 'Регистрация'}</h2>
                    {error && <div className="alert-error">{error}</div>}
                    <form onSubmit={handleAuth}>
                        <div style={{marginBottom: 12}}>
                            <label>Логин:</label>
                            <input required value={authForm.username}
                                   onChange={e => setAuthForm({...authForm, username: e.target.value})}/>
                        </div>
                        <div style={{marginBottom: 16}}>
                            <label>Пароль:</label>
                            <input type="password" required value={authForm.password}
                                   onChange={e => setAuthForm({...authForm, password: e.target.value})}/>
                        </div>
                        <button type="submit" className="btn btn-primary" style={{width: '100%'}}>
                            {authMode === 'login' ? 'Войти' : 'Зарегистрироваться'}
                        </button>
                    </form>
                    <div style={{marginTop: 12, textAlign: 'center'}}>
                        <button className="btn btn-secondary" onClick={() => {
                            clearAlerts();
                            setAuthMode(authMode === 'login' ? 'register' : 'login');
                        }}>
                            {authMode === 'login' ? 'Регистрация' : 'Назад ко входу'}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div>
            <div className="navbar">
                <div className="nav-links">
                    <strong style={{marginRight: 20}}>City IS</strong>
                    <button className={activeTab === 'cities' ? 'active' : ''} onClick={() => {
                        clearAlerts();
                        setActiveTab('cities');
                    }}>Города
                    </button>
                    <button className={activeTab === 'humans' ? 'active' : ''} onClick={() => {
                        clearAlerts();
                        setActiveTab('humans');
                    }}>Губернаторы
                    </button>
                    <button className={activeTab === 'coordinates' ? 'active' : ''} onClick={() => {
                        clearAlerts();
                        setActiveTab('coordinates');
                    }}>Координаты
                    </button>
                    <button className={activeTab === 'carCodes' ? 'active' : ''} onClick={() => {
                        clearAlerts();
                        setActiveTab('carCodes');
                    }}>Автокоды
                    </button>
                    <button className={activeTab === 'special' ? 'active' : ''} onClick={() => {
                        clearAlerts();
                        setActiveTab('special');
                    }}>Спец. операции
                    </button>
                </div>
                <div>
                    <span style={{marginRight: 12}}>Пользователь: {username}</span>
                    <button className="btn btn-danger" onClick={handleLogout}>Выйти</button>
                </div>
            </div>

            <div className="container">
                {error && <div className="alert-error">Ошибка: {error}</div>}
                {warning && <div className="alert-warning">Внимание: {warning}</div>}
                {success && <div className="alert-success">{success}</div>}

                {activeTab === 'cities' && (
                    <div className="card">
                        <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: 14
                        }}>
                            <h2 style={{margin: 0}}>Города ({filteredAndSortedCities.length})</h2>
                            <div style={{display: 'flex', gap: 10}}>
                                <form onSubmit={handleSearchById} style={{display: 'flex', gap: 6}}>
                                    <input type="number" placeholder="ID города" value={searchIdInput}
                                           onChange={e => setSearchIdInput(e.target.value)} style={{width: 140}}
                                           required/>
                                    <button type="submit" className="btn btn-secondary">Найти</button>
                                </form>
                                <button className="btn btn-primary" onClick={openCreateCityModal}>Добавить город
                                </button>
                            </div>
                        </div>

                        <div className="grid-2" style={{gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 12}}>
                            <input placeholder="Фильтр по названию" value={filters.name} onChange={e => {
                                setPage(1);
                                setFilters({...filters, name: e.target.value});
                            }}/>
                            <input placeholder="Фильтр по климату" value={filters.climate} onChange={e => {
                                setPage(1);
                                setFilters({...filters, climate: e.target.value});
                            }}/>
                            <input placeholder="Фильтр по правлению" value={filters.government} onChange={e => {
                                setPage(1);
                                setFilters({...filters, government: e.target.value});
                            }}/>
                            <input placeholder="Фильтр по губернатору" value={filters.governor} onChange={e => {
                                setPage(1);
                                setFilters({...filters, governor: e.target.value});
                            }}/>
                        </div>


                        <table>
                            <thead>
                            <tr>
                                <th onClick={() => toggleSort('id')}>ID</th>
                                <th onClick={() => toggleSort('name')}>Название</th>
                                <th onClick={() => toggleSort('capital')}>Столица</th>
                                <th onClick={() => toggleSort('area')}>Площадь</th>
                                <th onClick={() => toggleSort('population')}>Население</th>
                                <th onClick={() => toggleSort('metersAboveSeaLevel')}>Высота н.у.м.</th>
                                <th onClick={() => toggleSort('climate')}>Климат</th>
                                <th onClick={() => toggleSort('government')}>Правление</th>
                                <th>Координаты</th>
                                <th onClick={() => toggleSort('governor')}>Губернатор</th>
                                <th>Автокоды</th>
                                <th onClick={() => toggleSort('creationDate')}>Дата создания</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {paginatedCities.map(c => (
                                <tr key={c.id}>
                                    <td>{c.id}</td>
                                    <td><strong>{c.name}</strong></td>
                                    <td>{c.capital ? 'Да' : 'Нет'}</td>
                                    <td>{c.area}</td>
                                    <td>{c.population}</td>
                                    <td>{c.metersAboveSeaLevel ?? '-'}</td>
                                    <td>{c.climate}</td>
                                    <td>{c.government || '-'}</td>
                                    <td>{c.coordinates ? `(${c.coordinates.x}; ${c.coordinates.y}) [id:${c.coordinates.id}]` : '-'}</td>
                                    <td>{c.governor ? `${c.governor.name} ${c.governor.surname} (${c.governor.age})` : '-'}</td>
                                    <td>{(c.carCodeIds || []).map(code => <span key={code}
                                                                                className="badge">{code}</span>)}</td>
                                    <td>{c.creationDate ? new Date(c.creationDate).toLocaleString() : '-'}</td>
                                    <td>
                                        <div style={{display: 'flex', gap: '6px'}}>
                                            <button className="btn btn-secondary" style={{margin: 0}}
                                                    onClick={() => setViewCity(c)}>Инфо
                                            </button>
                                            <button className="btn btn-primary" style={{margin: 0}}
                                                    onClick={() => openEditCityModal(c)}>Изменить
                                            </button>
                                            <button className="btn btn-danger" style={{margin: 0}}
                                                    onClick={() => handleDeleteCity(c.id)}>Удалить
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            marginTop: '24px',
                            gap: '12px'
                        }}>
              <span style={{color: '#475569', fontWeight: '500'}}>
                Страница {page} из {totalPages}
              </span>
                            <div style={{display: 'flex', gap: '10px'}}>
                                <button className="btn btn-secondary" style={{margin: 0, padding: '8px 24px'}}
                                        disabled={page <= 1} onClick={() => setPage(page - 1)}>
                                    Назад
                                </button>
                                <button className="btn btn-secondary" style={{margin: 0, padding: '8px 24px'}}
                                        disabled={page >= totalPages} onClick={() => setPage(page + 1)}>
                                    Вперед
                                </button>
                            </div>
                        </div>

                    </div>
                )}


                {activeTab === 'humans' && (
                    <div className="card">
                        <h2>Губернаторы</h2>
                        <form onSubmit={async e => {
                            e.preventDefault();
                            clearAlerts();
                            try {
                                await apiFetch(humanForm.id ? `/humans/${humanForm.id}` : '/humans', {
                                    method: humanForm.id ? 'PUT' : 'POST',
                                    body: JSON.stringify({
                                        name: humanForm.name,
                                        surname: humanForm.surname,
                                        age: Number(humanForm.age)
                                    })
                                });
                                setHumanForm({id: null, name: '', surname: '', age: ''});
                                setSuccess('Сохранено');
                                loadAllData();
                            } catch (err) {
                                setError(err.message);
                            }
                        }} className="grid-2" style={{gridTemplateColumns: '1fr 1fr 1fr auto'}}>
                            <input placeholder="Имя" required value={humanForm.name}
                                   onChange={e => setHumanForm({...humanForm, name: e.target.value})}/>
                            <input placeholder="Фамилия" required value={humanForm.surname}
                                   onChange={e => setHumanForm({...humanForm, surname: e.target.value})}/>
                            <input type="number" placeholder="Возраст" required value={humanForm.age}
                                   onChange={e => setHumanForm({...humanForm, age: e.target.value})}/>
                            <button type="submit"
                                    className="btn btn-primary">{humanForm.id ? 'Обновить' : 'Добавить'}</button>
                        </form>
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Имя</th>
                                <th>Фамилия</th>
                                <th>Возраст</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {humans.map(h => (
                                <tr key={h.id}>
                                    <td>{h.id}</td>
                                    <td>{h.name}</td>
                                    <td>{h.surname}</td>
                                    <td>{h.age}</td>
                                    <td>
                                        <button className="btn btn-primary" onClick={() => setHumanForm(h)}>Изменить
                                        </button>
                                        <button className="btn btn-danger" onClick={async () => {
                                            clearAlerts();
                                            try {
                                                await apiFetch(`/humans/${h.id}`, {method: 'DELETE'});
                                                setSuccess('Удалено');
                                                loadAllData();
                                            } catch (err) {
                                                setError(err.message);
                                            }
                                        }}>Удалить
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {activeTab === 'coordinates' && (
                    <div className="card">
                        <h2>Координаты</h2>
                        <form onSubmit={async e => {
                            e.preventDefault();
                            clearAlerts();
                            try {
                                await apiFetch(coordForm.id ? `/coordinates/${coordForm.id}` : '/coordinates', {
                                    method: coordForm.id ? 'PUT' : 'POST',
                                    body: JSON.stringify({x: Number(coordForm.x), y: Number(coordForm.y)})
                                });
                                setCoordForm({id: null, x: '', y: ''});
                                setSuccess('Сохранено');
                                loadAllData();
                            } catch (err) {
                                setError(err.message);
                            }
                        }} className="grid-2" style={{gridTemplateColumns: '1fr 1fr auto'}}>
                            <input type="number" step="any" placeholder="X (макс 176)" required value={coordForm.x}
                                   onChange={e => setCoordForm({...coordForm, x: e.target.value})}/>
                            <input type="number" placeholder="Y" required value={coordForm.y}
                                   onChange={e => setCoordForm({...coordForm, y: e.target.value})}/>
                            <button type="submit"
                                    className="btn btn-primary">{coordForm.id ? 'Обновить' : 'Добавить'}</button>
                        </form>
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>X</th>
                                <th>Y</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {coordinates.map(c => (
                                <tr key={c.id}>
                                    <td>{c.id}</td>
                                    <td>{c.x}</td>
                                    <td>{c.y}</td>
                                    <td>
                                        <button className="btn btn-primary" onClick={() => setCoordForm(c)}>Изменить
                                        </button>
                                        <button className="btn btn-danger" onClick={async () => {
                                            clearAlerts();
                                            try {
                                                await apiFetch(`/coordinates/${c.id}`, {method: 'DELETE'});
                                                setSuccess('Удалено');
                                                loadAllData();
                                            } catch (err) {
                                                setError(err.message);
                                            }
                                        }}>Удалить
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {activeTab === 'carCodes' && (
                    <div className="card">
                        <h2>Автомобильные коды (1..1000)</h2>
                        <form onSubmit={async e => {
                            e.preventDefault();
                            clearAlerts();
                            try {
                                await apiFetch('/car-codes', {
                                    method: 'POST',
                                    body: JSON.stringify({id: Number(codeForm.id)})
                                });
                                setCodeForm({id: ''});
                                setSuccess('Код добавлен');
                                loadAllData();
                            } catch (err) {
                                setError(err.message);
                            }
                        }} style={{display: 'flex', gap: 10, maxWidth: 400}}>
                            <input type="number" placeholder="Код региона" required value={codeForm.id}
                                   onChange={e => setCodeForm({id: e.target.value})}/>
                            <button type="submit" className="btn btn-primary">Добавить</button>
                        </form>
                        <table>
                            <thead>
                            <tr>
                                <th>Код региона</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {carCodes.map(c => (
                                <tr key={c.id}>
                                    <td><span className="badge">{c.id}</span></td>
                                    <td>
                                        <button className="btn btn-danger" onClick={async () => {
                                            clearAlerts();
                                            try {
                                                await apiFetch(`/car-codes/${c.id}`, {method: 'DELETE'});
                                                setSuccess('Удалено');
                                                loadAllData();
                                            } catch (err) {
                                                setError(err.message);
                                            }
                                        }}>Удалить
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {activeTab === 'special' && (
                    <div className="card">
                        <h2>Специальные операции</h2>
                        {specialResult &&
                            <div className="alert-success">Результат: <strong>{specialResult}</strong></div>}

                        <div className="grid-2">
                            <div className="card" style={{border: '1px solid #e2e8f0'}}>
                                <h4>1. Сумма значений metersAboveSeaLevel</h4>
                                <button className="btn btn-primary" onClick={async () => {
                                    clearAlerts();
                                    try {
                                        const r = await apiFetch('/cities/special/sum-meters');
                                        setSpecialResult(`Сумма: ${r.sum}`);
                                    } catch (err) {
                                        setError(err.message);
                                    }
                                }}>Рассчитать
                                </button>
                            </div>

                            <div className="card" style={{border: '1px solid #e2e8f0'}}>
                                <h4>2. Количество городов с высотой больше заданной</h4>
                                <div style={{display: 'flex', gap: 8}}>
                                    <input type="number" step="any" value={thresholdInput}
                                           onChange={e => setThresholdInput(e.target.value)}/>
                                    <button className="btn btn-primary" onClick={async () => {
                                        clearAlerts();
                                        try {
                                            const r = await apiFetch(`/cities/special/count-meters-greater?threshold=${thresholdInput}`);
                                            setSpecialResult(`Количество: ${r.count}`);
                                        } catch (err) {
                                            setError(err.message);
                                        }
                                    }}>Найти
                                    </button>
                                </div>
                            </div>

                            <div className="card" style={{border: '1px solid #e2e8f0'}}>
                                <h4>3. Уникальные значения metersAboveSeaLevel</h4>
                                <button className="btn btn-primary" onClick={async () => {
                                    clearAlerts();
                                    try {
                                        const r = await apiFetch('/cities/special/unique-meters');
                                        setSpecialResult(`Уникальные значения: [${r.join(', ')}]`);
                                    } catch (err) {
                                        setError(err.message);
                                    }
                                }}>Получить
                                </button>
                            </div>

                            <div className="card" style={{border: '1px solid #e2e8f0'}}>
                                <h4>4. Переселить население города в наименьший</h4>
                                <div style={{display: 'flex', gap: 8}}>
                                    <select value={relocateCityId} onChange={e => setRelocateCityId(e.target.value)}>
                                        <option value="">Выберите город...</option>
                                        {cities.map(c => <option key={c.id} value={c.id}>{c.name} (id: {c.id},
                                            нас.: {c.population})</option>)}
                                    </select>
                                    <button className="btn btn-success" onClick={async () => {
                                        if (!relocateCityId) return;
                                        clearAlerts();
                                        try {
                                            const r = await apiFetch(`/cities/special/relocate-from/${relocateCityId}`, {method: 'POST'});
                                            setSpecialResult(r.message);
                                            loadAllData();
                                        } catch (err) {
                                            setError(err.message);
                                        }
                                    }}>Переселить
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="card" style={{border: '1px solid #e2e8f0'}}>
                            <h4>5. Переселить 50% жителей столицы в три города с наименьшим населением</h4>
                            <button className="btn btn-success" onClick={async () => {
                                clearAlerts();
                                try {
                                    const r = await apiFetch('/cities/special/relocate-capital', {method: 'POST'});
                                    setSpecialResult(r.message);
                                    loadAllData();
                                } catch (err) {
                                    setError(err.message);
                                }
                            }}>Переселить из столицы
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {viewCity && (
                <div className="modal-backdrop" onClick={() => setViewCity(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <h3>Город #{viewCity.id}: {viewCity.name}</h3>
                        <p><strong>Площадь:</strong> {viewCity.area} | <strong>Население:</strong> {viewCity.population}
                        </p>
                        <p><strong>Столица:</strong> {viewCity.capital ? 'Да' : 'Нет'} | <strong>Высота
                            н.у.м.:</strong> {viewCity.metersAboveSeaLevel ?? '-'}</p>
                        <p>
                            <strong>Климат:</strong> {viewCity.climate} | <strong>Правление:</strong> {viewCity.government || '-'}
                        </p>
                        <p><strong>Дата основания:</strong> {viewCity.establishmentDate || '-'}</p>
                        <p><strong>Дата создания:</strong> {viewCity.creationDate}</p>
                        <hr/>
                        <h4>Связанные объекты:</h4>
                        <p><strong>Координаты (ID {viewCity.coordinates?.id}):</strong> X = {viewCity.coordinates?.x}, Y
                            = {viewCity.coordinates?.y}</p>
                        <p><strong>Губернатор
                            (ID {viewCity.governor?.id}):</strong> {viewCity.governor?.name} {viewCity.governor?.surname},
                            возраст: {viewCity.governor?.age}</p>
                        <p><strong>Автокоды:</strong> {(viewCity.carCodeIds || []).join(', ') || 'нет'}</p>
                        <div style={{marginTop: 16, textAlign: 'right'}}>
                            <button className="btn btn-primary" onClick={() => {
                                const c = viewCity;
                                setViewCity(null);
                                openEditCityModal(c);
                            }}>Редактировать
                            </button>
                            <button className="btn btn-secondary" onClick={() => setViewCity(null)}>Закрыть</button>
                        </div>
                    </div>
                </div>
            )}

            {cityModalOpen && (
                <div className="modal-backdrop">
                    <div className="modal" style={{width: 700}}>
                        <h3>{editingCityId ? `Редактирование города #${editingCityId}` : 'Новый город'}</h3>
                        <form onSubmit={handleSaveCity}>
                            <div className="grid-2">
                                <div>
                                    <label>Название*:</label>
                                    <input required value={cityForm.name}
                                           onChange={e => setCityForm({...cityForm, name: e.target.value})}/>
                                </div>
                                <div>
                                    <label>Площадь*:</label>
                                    <input type="number" step="any" required value={cityForm.area}
                                           onChange={e => setCityForm({...cityForm, area: e.target.value})}/>
                                </div>
                                <div>
                                    <label>Население*:</label>
                                    <input type="number" required value={cityForm.population}
                                           onChange={e => setCityForm({...cityForm, population: e.target.value})}/>
                                </div>
                                <div>
                                    <label>Высота над уровнем моря:</label>
                                    <input type="number" step="any" value={cityForm.metersAboveSeaLevel}
                                           onChange={e => setCityForm({
                                               ...cityForm,
                                               metersAboveSeaLevel: e.target.value
                                           })}/>
                                </div>
                                <div>
                                    <label>Климат*:</label>
                                    <select value={cityForm.climate}
                                            onChange={e => setCityForm({...cityForm, climate: e.target.value})}>
                                        {CLIMATES.map(cl => <option key={cl} value={cl}>{cl}</option>)}
                                    </select>
                                </div>
                                <div>
                                    <label>Форма правления:</label>
                                    <select value={cityForm.government}
                                            onChange={e => setCityForm({...cityForm, government: e.target.value})}>
                                        <option value="">Не выбрано</option>
                                        {GOVERNMENTS.map(g => <option key={g} value={g}>{g}</option>)}
                                    </select>
                                </div>
                                <div>
                                    <label>Дата основания:</label>
                                    <input type="datetime-local" value={cityForm.establishmentDate}
                                           onChange={e => setCityForm({
                                               ...cityForm,
                                               establishmentDate: e.target.value
                                           })}/>
                                </div>
                                <div style={{display: 'flex', alignItems: 'center', marginTop: 22}}>
                                    <input type="checkbox" style={{width: 20, marginRight: 8}}
                                           checked={cityForm.capital}
                                           onChange={e => setCityForm({...cityForm, capital: e.target.checked})}/>
                                    <label>Столица</label>
                                </div>
                            </div>

                            <hr/>

                            <div style={{marginBottom: 16}}>
                                <label><strong>Автомобильные коды:</strong></label>
                                <div style={{
                                    border: '1px solid #cbd5e1',
                                    padding: 8,
                                    borderRadius: 6,
                                    maxHeight: 100,
                                    overflowY: 'auto',
                                    marginBottom: 8,
                                    background: '#f8fafc'
                                }}>
                                    {carCodes.length === 0 ? <span style={{color: '#94a3b8', fontSize: 13}}>Нет доступных кодов в системе</span> :
                                        carCodes.map(code => (
                                            <label key={code.id} style={{
                                                display: 'inline-flex',
                                                alignItems: 'center',
                                                marginRight: 12,
                                                fontSize: 14
                                            }}>
                                                <input
                                                    type="checkbox"
                                                    style={{width: 'auto', marginRight: 4}}
                                                    checked={cityForm.selectedCarCodes.includes(code.id)}
                                                    onChange={() => toggleCarCode(code.id)}
                                                />
                                                {code.id}
                                            </label>
                                        ))
                                    }
                                </div>
                                <input placeholder="Или введите новые через запятую (напр. 77, 99)"
                                       value={cityForm.newCarCodesText}
                                       onChange={e => setCityForm({...cityForm, newCarCodesText: e.target.value})}/>
                            </div>

                            <hr/>

                            <div style={{marginBottom: 12}}>
                                <label><strong>Координаты*:</strong> </label>
                                <select value={cityForm.coordMode}
                                        onChange={e => setCityForm({...cityForm, coordMode: e.target.value})}
                                        style={{width: 'auto', marginLeft: 8, marginBottom: 8}}>
                                    <option value="existing">Выбрать существующие</option>
                                    <option value="new">Создать новые</option>
                                </select>
                                {cityForm.coordMode === 'existing' ? (
                                    <select required value={cityForm.coordinatesId}
                                            onChange={e => setCityForm({...cityForm, coordinatesId: e.target.value})}>
                                        <option value="">Выберите координаты...</option>
                                        {coordinates.map(c => <option key={c.id} value={c.id}>ID {c.id}:
                                            ({c.x}; {c.y})</option>)}
                                    </select>
                                ) : (
                                    <div className="grid-2">
                                        <input type="number" step="any" placeholder="X" required value={cityForm.newX}
                                               onChange={e => setCityForm({...cityForm, newX: e.target.value})}/>
                                        <input type="number" placeholder="Y" required value={cityForm.newY}
                                               onChange={e => setCityForm({...cityForm, newY: e.target.value})}/>
                                    </div>
                                )}
                            </div>

                            <div style={{marginBottom: 16}}>
                                <label><strong>Губернатор*:</strong> </label>
                                <select value={cityForm.govMode}
                                        onChange={e => setCityForm({...cityForm, govMode: e.target.value})}
                                        style={{width: 'auto', marginLeft: 8, marginBottom: 8}}>
                                    <option value="existing">Выбрать существующего</option>
                                    <option value="new">Создать нового</option>
                                </select>
                                {cityForm.govMode === 'existing' ? (
                                    <select required value={cityForm.governorId}
                                            onChange={e => setCityForm({...cityForm, governorId: e.target.value})}>
                                        <option value="">Выберите губернатора...</option>
                                        {humans.map(h => <option key={h.id}
                                                                 value={h.id}>ID {h.id}: {h.name} {h.surname} ({h.age})</option>)}
                                    </select>
                                ) : (
                                    <div className="grid-2" style={{gridTemplateColumns: '1fr 1fr 1fr'}}>
                                        <input placeholder="Имя" required value={cityForm.newGovName}
                                               onChange={e => setCityForm({...cityForm, newGovName: e.target.value})}/>
                                        <input placeholder="Фамилия" required value={cityForm.newGovSurname}
                                               onChange={e => setCityForm({
                                                   ...cityForm,
                                                   newGovSurname: e.target.value
                                               })}/>
                                        <input type="number" placeholder="Возраст" required value={cityForm.newGovAge}
                                               onChange={e => setCityForm({...cityForm, newGovAge: e.target.value})}/>
                                    </div>
                                )}
                            </div>

                            <div style={{textAlign: 'right'}}>
                                <button type="button" className="btn btn-secondary"
                                        onClick={() => setCityModalOpen(false)}>Отмена
                                </button>
                                <button type="submit" className="btn btn-primary">Сохранить</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
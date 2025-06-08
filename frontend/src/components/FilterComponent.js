import React from 'react';

function FilterComponent({ year, setYear, month, setMonth, minYear, maxYear }) {
    const months = [
        { value: 0, label: 'Todos' }, { value: 1, label: 'Ene' }, { value: 2, label: 'Feb' },
        { value: 3, label: 'Mar' }, { value: 4, label: 'Abr' }, { value: 5, label: 'May' },
        { value: 6, label: 'Jun' }, { value: 7, label: 'Jul' }, { value: 8, label: 'Ago' },
        { value: 9, label: 'Sep' }, { value: 10, label: 'Oct' }, { value: 11, label: 'Nov' },
        { value: 12, label: 'Dic' }
    ];

    return (
        <div className="filter-container">
            <h3>Filtros de Incendios</h3>
            <div className="filter-item">
                <label htmlFor="year-slider">Año: {year}</label>
                <input
                    id="year-slider"
                    type="range"
                    min={minYear}
                    max={maxYear}
                    value={year}
                    onChange={(e) => setYear(parseInt(e.target.value))}
                    style={{ width: '100%' }}
                />
            </div>
            <div className="filter-item">
                <label htmlFor="month-select">Mes: </label>
                <select id="month-select" value={month} onChange={(e) => setMonth(parseInt(e.target.value))}>
                    {months.map(m => <option key={m.value} value={m.value}>{m.label}</option>)}
                </select>
            </div>
        </div>
    );
}

export default FilterComponent;
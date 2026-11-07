// Construye SALVADOR-DIEGO-AP4.docx a partir de contenido.md (marcado propio).
// Uso: node build.js [paginas.json]
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell, WidthType, ShadingType,
  AlignmentType, HeadingLevel, PageBreak, Header, Footer, PageNumber, BorderStyle, LevelFormat,
  PositionalTab, PositionalTabAlignment, PositionalTabRelativeTo, PositionalTabLeader, TabStopType, PageOrientation,
} = require('docx');
const sizeOf = (f) => { // PNG: ancho y alto del encabezado IHDR
  const b = fs.readFileSync(f); return { w: b.readUInt32BE(16), h: b.readUInt32BE(20) };
};

const FONT = 'Calibri', MONO = 'Consolas', VERDE = '004D40', GRIS = '595959';
const PAGE_W_CM = 21 - 2 * 2.2; // márgenes laterales de 2,2 cm
const CM = 567; // twips por cm
const TABLE_W = Math.round(PAGE_W_CM * CM);
const paginas = process.argv[2] && fs.existsSync(process.argv[2]) ? JSON.parse(fs.readFileSync(process.argv[2])) : {};

const lineas = fs.readFileSync(path.join(__dirname, 'contenido.md'), 'utf8').split('\n');

// --- texto en línea: **negrita** y *cursiva* -----------------------------------------------
function runs(texto, base = {}) {
  const out = [];
  const re = /(\*\*[^*]+\*\*|\*[^*]+\*)/g;
  let i = 0, m;
  while ((m = re.exec(texto))) {
    if (m.index > i) out.push(new TextRun({ text: texto.slice(i, m.index), ...base }));
    const t = m[0];
    if (t.startsWith('**')) out.push(new TextRun({ text: t.slice(2, -2), bold: true, ...base }));
    else out.push(new TextRun({ text: t.slice(1, -1), italics: true, ...base }));
    i = m.index + t.length;
  }
  if (i < texto.length) out.push(new TextRun({ text: texto.slice(i), ...base }));
  return out;
}
const p = (texto, opt = {}) => new Paragraph({ children: runs(texto, opt.run || {}), spacing: { after: 120 },
  alignment: AlignmentType.JUSTIFIED, ...opt.par });
const leyenda = (texto) => new Paragraph({ children: [new TextRun({ text: texto, bold: true, size: 20 })],
  spacing: { before: 160, after: 60 }, keepNext: true, keepLines: true });
const fuente = (texto = 'Fuente: elaboración propia.') => new Paragraph({
  children: [new TextRun({ text: texto, italics: true, size: 18, color: GRIS })], spacing: { before: 40, after: 200 } });

// --- elementos --------------------------------------------------------------------------------
let nFig = 0, nTab = 0;
const figuras = [], tablas = [], titulos = [];
const body = [];
const borde = { style: BorderStyle.SINGLE, size: 4, color: 'A6A6A6' };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };

function tabla(caption, widths, filas) {
  nTab++; tablas.push(`Tabla ${nTab}: ${caption}`);
  body.push(leyenda(`Tabla ${nTab}: ${caption}`));
  const total = widths.reduce((a, b) => a + b, 0);
  const cols = widths.map((w) => Math.round((w / total) * TABLE_W));
  cols[cols.length - 1] += TABLE_W - cols.reduce((a, b) => a + b, 0);
  const rows = filas.map((celdas, ri) => new TableRow({
    tableHeader: ri === 0, cantSplit: true,
    children: celdas.map((c, ci) => new TableCell({
      width: { size: cols[ci], type: WidthType.DXA }, borders: bordes,
      shading: ri === 0 ? { type: ShadingType.CLEAR, fill: VERDE, color: 'auto' }
        : (ri % 2 === 0 ? { type: ShadingType.CLEAR, fill: 'F2F7F6', color: 'auto' } : undefined),
      margins: { top: 50, bottom: 50, left: 90, right: 90 },
      children: [new Paragraph({ children: runs(c, ri === 0 ? { bold: true, color: 'FFFFFF', size: 19 } : { size: 19 }),
        spacing: { after: 0 } })],
    })),
  }));
  body.push(new Table({ width: { size: TABLE_W, type: WidthType.DXA }, columnWidths: cols, rows }));
  body.push(fuente());
}

function figura(archivo, caption, anchoCm, apaisada) {
  nFig++; figuras.push(`Figura ${nFig}: ${caption}`);
  const f = path.join(__dirname, 'fig', archivo);
  const { w, h } = sizeOf(f);
  if (apaisada) body.push({ __seccion: 'apaisada' }); // la figura ocupa una página horizontal propia
  let ancho = Math.min(anchoCm, apaisada ? 29.7 - 4.4 : PAGE_W_CM) * 37.795; // px a 96 dpi
  let alto = ancho * h / w;
  const maxAlto = (apaisada ? 14.2 : 21) * 37.795; // alto disponible
  if (alto > maxAlto) { ancho = ancho * maxAlto / alto; alto = maxAlto; }
  body.push(leyenda(`Figura ${nFig}: ${caption}`));
  body.push(new Paragraph({ alignment: AlignmentType.CENTER, keepNext: true, spacing: { after: 40 },
    children: [new ImageRun({ type: 'png', data: fs.readFileSync(f), transformation: { width: Math.round(ancho), height: Math.round(alto) } })] }));
  body.push(fuente());
  if (apaisada) body.push({ __seccion: 'vertical' });
}

function codigo(titulo, lineasCodigo) {
  body.push(new Paragraph({ children: [new TextRun({ text: titulo, bold: true, size: 18, color: VERDE })],
    spacing: { before: 120, after: 40 }, keepNext: true }));
  lineasCodigo.forEach((l, i) => body.push(new Paragraph({
    children: [new TextRun({ text: l.length ? l : ' ', font: MONO, size: 16 })],
    shading: { type: ShadingType.CLEAR, fill: 'F4F4F4', color: 'auto' },
    border: { left: { style: BorderStyle.SINGLE, size: 12, color: VERDE, space: 6 } },
    spacing: { after: 0, line: 240, lineRule: 'auto' }, keepNext: i < lineasCodigo.length - 1, keepLines: true,
    indent: { left: 120 },
  })));
  body.push(new Paragraph({ children: [], spacing: { after: 120 } }));
}

function indice() {
  body.push(new Paragraph({ heading: HeadingLevel.HEADING_1, pageBreakBefore: false, children: [new TextRun('Índice')] }));
  for (const t of titulosPrevios()) {
    const pag = paginas[t.texto] ?? '';
    body.push(new Paragraph({
      indent: { left: t.nivel === 2 ? 360 : 0 }, spacing: { after: t.nivel === 1 ? 60 : 20, before: t.nivel === 1 ? 80 : 0 },
      tabStops: [{ type: TabStopType.RIGHT, position: TABLE_W, leader: 'dot' }],
      children: [new TextRun({ text: t.texto, bold: t.nivel === 1, size: t.nivel === 1 ? 22 : 21 }),
        new TextRun({ text: `\t${pag}`, bold: t.nivel === 1, size: t.nivel === 1 ? 22 : 21 })],
    }));
  }
}
function titulosPrevios() { // recorre el contenido para armar el índice antes de generar el cuerpo
  const res = [];
  for (const l of lineas) {
    if (l.startsWith('# ')) res.push({ nivel: 1, texto: l.slice(2).trim() });
    else if (l.startsWith('## ')) res.push({ nivel: 2, texto: l.slice(3).trim() });
  }
  return res;
}

function portada() {
  const c = (t, o = {}) => new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: o.after ?? 120 },
    children: [new TextRun({ text: t, font: FONT, size: o.size || 24, bold: !!o.bold, color: o.color, italics: !!o.it })] });
  body.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 600, after: 300 },
    children: [new ImageRun({ type: 'png', data: fs.readFileSync(path.join(__dirname, 'fig', 'logo.png')), transformation: { width: 96, height: 120 } })] }));
  body.push(c('UNIVERSIDAD SIGLO 21', { size: 28, bold: true, color: VERDE }));
  body.push(c('Licenciatura en Informática', { size: 24, after: 900 }));
  body.push(c('TALLERNET', { size: 64, bold: true, color: VERDE, after: 160 }));
  body.push(c('Sistema de gestión multisede para talleres mecánicos', { size: 30, after: 200 }));
  body.push(c('Proyecto integrador: versión final del prototipo', { size: 24, it: true, after: 900 }));
  const datos = [['Trabajo Práctico', 'N.º 4 (AP4). Entrega incremental e integradora de AP1, AP2 y AP3'],
    ['Asignatura', 'Seminario de Práctica de Informática'], ['Autor', 'Diego Salvador'],
    ['Organización', 'Talleres Ruiz Díaz (Ciudad Autónoma de Buenos Aires)'], ['Fecha de entrega', 'Noviembre de 2026'],
    ['Versión del documento', '3 (7 de noviembre de 2026): incorpora las correcciones de la revisión contra la rúbrica']];
  for (const [k, v] of datos) {
    body.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 80 },
      children: [new TextRun({ text: `${k}: `, bold: true, size: 23 }), new TextRun({ text: v, size: 23 })] }));
  }
  body.push(new Paragraph({ children: [new PageBreak()] }));
}

// --- recorrido del marcado ---------------------------------------------------------------------
let i = 0;
let primerH1 = true;
while (i < lineas.length) {
  const l = lineas[i];
  if (l.trim() === '') { i++; continue; }
  if (l.trim() === 'PORTADA') { portada(); i++; continue; }
  if (l.trim() === '[[INDICE]]') { indice(); i++; continue; }
  if (l.startsWith('# ')) {
    const t = l.slice(2).trim(); titulos.push({ nivel: 1, texto: t });
    body.push(new Paragraph({ heading: HeadingLevel.HEADING_1, pageBreakBefore: true, children: [new TextRun(t)] }));
    i++; continue;
  }
  if (l.startsWith('## ')) {
    const t = l.slice(3).trim(); titulos.push({ nivel: 2, texto: t });
    body.push(new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(t)] })); i++; continue;
  }
  if (l.startsWith('### ')) { body.push(new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun(l.slice(4).trim())] })); i++; continue; }
  if (l.startsWith('TABLA: ')) {
    const cap = l.slice(7).trim(); i++;
    let widths = null;
    if (lineas[i].startsWith('WIDTHS:')) { widths = lineas[i].slice(7).split(',').map(Number); i++; }
    const filas = [];
    while (i < lineas.length && lineas[i].startsWith('|')) {
      filas.push(lineas[i].trim().replace(/^\||\|$/g, '').split(' | ').map((s) => s.trim())); i++;
    }
    tabla(cap, widths || filas[0].map(() => 1), filas); continue;
  }
  if (l.startsWith('FIG: ')) {
    const [arch, cap, ancho, modo] = l.slice(5).split('|').map((s) => s.trim());
    figura(arch, cap, Number(ancho || 15), modo === 'apaisada'); i++; continue;
  }
  if (l.startsWith('CODE: ')) {
    const t = l.slice(6).trim(); i++; const cod = [];
    while (lineas[i] !== 'END') { cod.push(lineas[i]); i++; }
    i++; codigo(t, cod); continue;
  }
  if (l.startsWith('REF: ')) {
    body.push(new Paragraph({ children: runs(l.slice(5)), indent: { left: 567, hanging: 567 }, spacing: { after: 100 } }));
    i++; continue;
  }
  if (l.startsWith('- ')) {
    body.push(new Paragraph({ children: runs(l.slice(2)), numbering: { reference: 'vinetas', level: 0 },
      spacing: { after: 60 }, alignment: AlignmentType.JUSTIFIED })); i++; continue;
  }
  body.push(p(l)); i++;
}

const encabezado = (ancho = TABLE_W) => new Header({ children: [new Paragraph({
  border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: 'A6A6A6', space: 4 } },
  tabStops: [{ type: TabStopType.RIGHT, position: ancho }],
  children: [new TextRun({ text: 'TALLERNET — Trabajo Práctico N.º 4 (AP4) · Versión 3', size: 17, color: GRIS }),
    new TextRun({ text: '\tUNIVERSIDAD SIGLO 21', size: 17, color: GRIS })] })] });
const pie = () => new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: 'Seminario de Práctica de Informática · Página ', size: 17, color: GRIS }),
    new TextRun({ children: [PageNumber.CURRENT], size: 17, color: GRIS })] })] });
function secciones() { // separa el cuerpo en secciones verticales y apaisadas
  const res = []; let actual = [], modo = 'vertical';
  const cerrar = () => {
    if (!actual.length) return;
    const primera = res.length === 0;
    const margen = { top: 1300, bottom: 1200, left: Math.round(2.2 * CM), right: Math.round(2.2 * CM), header: 600, footer: 600 };
    res.push({ properties: { titlePage: primera, page: { size: { width: 11906, height: 16838,
        orientation: modo === 'apaisada' ? PageOrientation.LANDSCAPE : PageOrientation.PORTRAIT }, margin: margen } },
      headers: primera ? { default: encabezado(), first: new Header({ children: [new Paragraph({ children: [] })] }) } : { default: encabezado(modo === 'apaisada' ? Math.round((29.7 - 4.4) * CM) : TABLE_W) },
      footers: primera ? { default: pie(), first: new Footer({ children: [new Paragraph({ children: [] })] }) } : { default: pie() },
      children: actual });
    actual = [];
  };
  for (const b of body) {
    if (b && b.__seccion) { cerrar(); modo = b.__seccion; continue; }
    actual.push(b);
  }
  cerrar();
  return res;
}

const doc = new Document({
  creator: 'Diego Salvador', title: 'TALLERNET - Trabajo Práctico N.º 4 (AP4) - Versión 3', revision: 3,
  styles: {
    default: { document: { run: { font: FONT, size: 22 } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { font: FONT, size: 32, bold: true, color: VERDE }, paragraph: { spacing: { before: 0, after: 200 }, keepNext: true, outlineLevel: 0 } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { font: FONT, size: 26, bold: true, color: VERDE }, paragraph: { spacing: { before: 280, after: 120 }, keepNext: true, outlineLevel: 1 } },
      { id: 'Heading3', name: 'Heading 3', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { font: FONT, size: 23, bold: true }, paragraph: { spacing: { before: 200, after: 100 }, keepNext: true, outlineLevel: 2 } },
    ],
  },
  numbering: { config: [{ reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
    style: { run: { font: FONT }, paragraph: { indent: { left: 567, hanging: 283 } } } }] }] },
  sections: secciones(),
});

Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync(path.join(__dirname, 'SALVADOR-DIEGO-AP4.docx'), buf);
  fs.writeFileSync(path.join(__dirname, 'numeracion.txt'), [...tablas, '', ...figuras].join('\n'));
  console.log(`ok: ${nTab} tablas, ${nFig} figuras, ${titulos.length} títulos`);
});

import { useEffect, useRef } from 'react';
import {
  CanvasTexture,
  WebGLRenderer,
  Scene,
  OrthographicCamera,
  Mesh,
  SphereGeometry,
  MeshPhongMaterial,
  Color,
  DirectionalLight,
  AmbientLight,
  Clock,
} from 'three';

// ── Minimal 2D Simplex-style noise (no dependency) ───────────────────────────
function noise2D(x: number, y: number): number {
  const X = Math.floor(x) & 255;
  const Y = Math.floor(y) & 255;
  x -= Math.floor(x);
  y -= Math.floor(y);
  const u = fade(x);
  const v = fade(y);
  const a = p[X] + Y;
  const aa = p[a];
  const ab = p[a + 1];
  const b = p[X + 1] + Y;
  const ba = p[b];
  const bb = p[b + 1];
  return lerp(
    v,
    lerp(u, grad(p[aa], x, y), grad(p[ba], x - 1, y)),
    lerp(u, grad(p[ab], x, y - 1), grad(p[bb], x - 1, y - 1))
  );
}

function fade(t: number) {
  return t * t * t * (t * (t * 6 - 15) + 10);
}
function lerp(t: number, a: number, b: number) {
  return a + t * (b - a);
}
function grad(hash: number, x: number, y: number) {
  const h = hash & 3;
  const u = h < 2 ? x : y;
  const v = h < 2 ? y : x;
  return (h & 1 ? -u : u) + (h & 2 ? -v : v);
}

// Permutation table — fixed seed for deterministic texture
const pRaw = [
  151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
  8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203,
  117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165,
  71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92,
  41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
  89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217,
  226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58,
  17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155,
  167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218,
  246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14,
  239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150,
  254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180,
];
const p = new Array(512);
for (let i = 0; i < 512; i++) p[i] = pRaw[i & 255];

// Fractional Brownian Motion — stacks octaves for more organic shapes
function fbm(x: number, y: number, octaves = 5): number {
  let val = 0,
    amp = 0.5,
    freq = 1,
    max = 0;
  for (let i = 0; i < octaves; i++) {
    val += noise2D(x * freq, y * freq) * amp;
    max += amp;
    amp *= 0.5;
    freq *= 2.1;
  }
  return val / max;
}

function makePlanetTexture(): CanvasTexture {
  const size = 1024;
  const c = document.createElement('canvas');
  c.width = c.height = size;
  const ctx = c.getContext('2d')!;
  const img = ctx.createImageData(size, size);
  const d = img.data;

  // Colours
  const ocean = [13, 72, 120];
  const shallows = [30, 100, 160];
  const lowland = [58, 130, 65];
  const highland = [80, 160, 80];
  const mountain = [120, 110, 80];
  const snow = [220, 235, 255];

  const LAND_THRESHOLD = 0.08; // positive fbm = land
  const scale = 2.8; // zoom level of the noise

  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      // Map pixel to sphere UV — wraps horizontally so no seam
      const u = x / size;
      const v = y / size;

      // Convert UV → 3D point on sphere so noise wraps seamlessly
      const lon = u * Math.PI * 2;
      const lat = (v - 0.5) * Math.PI;
      const nx = Math.cos(lat) * Math.cos(lon) * scale;
      const ny = Math.cos(lat) * Math.sin(lon) * scale;
      const nz = Math.sin(lat) * scale;

      // Use 3D-ish noise via two 2D slices combined — eliminates seam
      const h = fbm(nx + nz * 0.3, ny + nz * 0.3);

      // Polar ice blending
      const pole = Math.abs(v - 0.5) * 2; // 0 at equator, 1 at poles
      const iceBlend = Math.max(0, (pole - 0.75) / 0.25);

      let r: number, g: number, b: number;

      if (h < LAND_THRESHOLD - 0.12) {
        [r, g, b] = ocean;
      } else if (h < LAND_THRESHOLD) {
        // Shallow water strip along coast
        const t = (h - (LAND_THRESHOLD - 0.12)) / 0.12;
        [r, g, b] = [
          ocean[0] + (shallows[0] - ocean[0]) * t,
          ocean[1] + (shallows[1] - ocean[1]) * t,
          ocean[2] + (shallows[2] - ocean[2]) * t,
        ];
      } else if (h < LAND_THRESHOLD + 0.15) {
        [r, g, b] = lowland;
      } else if (h < LAND_THRESHOLD + 0.28) {
        const t = (h - (LAND_THRESHOLD + 0.15)) / 0.13;
        [r, g, b] = [
          lowland[0] + (highland[0] - lowland[0]) * t,
          lowland[1] + (highland[1] - lowland[1]) * t,
          lowland[2] + (highland[2] - lowland[2]) * t,
        ];
      } else if (h < LAND_THRESHOLD + 0.4) {
        const t = (h - (LAND_THRESHOLD + 0.28)) / 0.12;
        [r, g, b] = [
          highland[0] + (mountain[0] - highland[0]) * t,
          highland[1] + (mountain[1] - highland[1]) * t,
          highland[2] + (mountain[2] - highland[2]) * t,
        ];
      } else {
        [r, g, b] = mountain;
      }

      // Blend to snow near poles
      r = r + (snow[0] - r) * iceBlend;
      g = g + (snow[1] - g) * iceBlend;
      b = b + (snow[2] - b) * iceBlend;

      const i = (y * size + x) * 4;
      d[i] = r;
      d[i + 1] = g;
      d[i + 2] = b;
      d[i + 3] = 255;
    }
  }

  ctx.putImageData(img, 0, 0);
  return new CanvasTexture(c);
}

export function Planet() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    let alive = true;

    const renderer = new WebGLRenderer({ canvas, alpha: true, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    const scene = new Scene();

    const makeCamera = () => {
      const W = window.innerWidth;
      const H = window.innerHeight;
      const aspect = W / H;
      const frustH = 10;
      return new OrthographicCamera(
        (-frustH * aspect) / 2,
        (frustH * aspect) / 2,
        frustH / 2,
        -frustH / 2,
        0.1,
        100
      );
    };

    let camera = makeCamera();
    camera.position.z = 10;

    const radius = 5.2;
    const sphere = new Mesh(
      new SphereGeometry(radius, 64, 64),
      new MeshPhongMaterial({
        map: makePlanetTexture(),
        specular: new Color(0x224488),
        shininess: 40,
      })
    );
    sphere.position.y = -radius * 0.82;
    scene.add(sphere);

    const sun = new DirectionalLight(0xfff5e0, 2.2);
    sun.position.set(4, 5, 6);
    scene.add(sun);
    scene.add(new AmbientLight(0x223366, 1.2));

    renderer.setSize(window.innerWidth, window.innerHeight);

    const onResize = () => {
      renderer.setSize(window.innerWidth, window.innerHeight);
      camera = makeCamera();
      camera.position.z = 10;
    };
    window.addEventListener('resize', onResize);

    const clock = new Clock();
    const animate = () => {
      if (!alive) return;
      requestAnimationFrame(animate);
      sphere.rotation.y = clock.getElapsedTime() * 0.05;
      renderer.render(scene, camera);
    };
    animate();

    return () => {
      alive = false;
      window.removeEventListener('resize', onResize);
      renderer.dispose();
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }}
    />
  );
}

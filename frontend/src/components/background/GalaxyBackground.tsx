import { useEffect, useRef, useMemo, ReactNode } from 'react';
import { Planet } from './Planet';
import { Camera, Color, Mesh, PlaneGeometry, Scene, ShaderMaterial, WebGLRenderer } from 'three';

interface Star {
  id: number;
  x: number; // % from left
  y: number; // % from top
  size: number; // px
  opacity: number; // base opacity
  duration: number; // animation duration in seconds
  delay: number; // animation delay in seconds
}

function generateStars(count: number): Star[] {
  return Array.from({ length: count }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: 1 + Math.random() * 2.5,
    opacity: 0.4 + Math.random() * 0.6,
    duration: 2 + Math.random() * 4,
    delay: Math.random() * 6,
  }));
}

export function GalaxyBackground({ children }: Readonly<{ children?: ReactNode }>) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const stars = useMemo(() => generateStars(250), []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    let alive = true;

    const renderer = new WebGLRenderer({ canvas });
    renderer.setSize(window.innerWidth, window.innerHeight);

    const geometry = new PlaneGeometry(2, 2);
    const material = new ShaderMaterial({
      uniforms: {
        topColor: { value: new Color(0x0a0015) },
        midColor: { value: new Color(0x0d1a6e) },
        bottomColor: { value: new Color(0x1a0a4a) },
      },
      vertexShader: `
        varying vec2 vUv;
        void main() {
          vUv = uv;
          gl_Position = vec4(position, 1.0);
        }
      `,
      fragmentShader: `
        uniform vec3 topColor;
        uniform vec3 midColor;
        uniform vec3 bottomColor;
        varying vec2 vUv;
        void main() {
          vec3 col = mix(bottomColor, midColor, smoothstep(0.0, 0.5, vUv.y));
          col = mix(col, topColor, smoothstep(0.4, 1.0, vUv.y));
          gl_FragColor = vec4(col, 1.0);
        }
      `,
      depthWrite: false,
    });

    const scene = new Scene();
    const camera = new Camera();
    scene.add(new Mesh(geometry, material));

    const onResize = () => renderer.setSize(window.innerWidth, window.innerHeight);
    window.addEventListener('resize', onResize);

    const animate = () => {
      if (!alive) return;
      requestAnimationFrame(animate);
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
    <>
      <div
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 0,
          backgroundColor: '#0a0015',
        }}
      >
        {/* Gradient sky */}
        <canvas
          ref={canvasRef}
          style={{
            position: 'absolute',
            inset: 0,
            width: '100%',
            height: '100%',
          }}
        />

        {/* Stars */}
        <div style={{ position: 'absolute', inset: 0, overflow: 'hidden' }}>
          <style>{`
          @keyframes twinkle {
            0%, 100% { opacity: var(--star-opacity); transform: scale(1); }
            50%       { opacity: calc(var(--star-opacity) * 0.15); transform: scale(0.85); }
          }
        `}</style>
          {stars.map((star) => (
            <div
              key={star.id}
              style={
                {
                  position: 'absolute',
                  left: `${star.x}%`,
                  top: `${star.y}%`,
                  width: `${star.size}px`,
                  height: `${star.size}px`,
                  borderRadius: '50%',
                  backgroundColor: '#ffffff',
                  '--star-opacity': star.opacity,
                  opacity: star.opacity,
                  boxShadow: `0 0 ${star.size * 2}px rgba(255,255,255,${star.opacity * 0.6})`,
                  animation: `twinkle ${star.duration}s ${star.delay}s ease-in-out infinite`,
                } as React.CSSProperties
              }
            />
          ))}
        </div>
        <Planet />
      </div>
      <main
        style={{ position: 'relative', zIndex: 1 }}
        className="flex min-h-screen flex-col items-center justify-center gap-8 p-8 select-none"
      >
        {children}
      </main>
    </>
  );
}

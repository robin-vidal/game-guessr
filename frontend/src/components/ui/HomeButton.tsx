import { useNavigate } from 'react-router';
import { Button } from './Button';
import { House } from 'lucide-react';

export default function HomeButton() {
  const navigate = useNavigate();
  return (
    <Button
      variant="secondary"
      size="icon"
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        margin: '2rem',
        padding: 4,
      }}
      onClick={() => navigate('/')}
    >
      <House />
    </Button>
  );
}

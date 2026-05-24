// components/SkeletonCard.jsx
function Skeleton({ width = '100%', height = 20, style = {} }) {
  return (
    <div style={{
      width, height, borderRadius: 4,
      background: 'linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)',
      backgroundSize: '200% 100%',
      animation: 'shimmer 1.5s infinite',
      ...style,
    }} />
  );
}

// 在 CSS 加入：
// @keyframes shimmer { from { background-position: 200% 0; } to { background-position: -200% 0; } }

function SkeletonCard() {
  return (
    <div style={{ padding: 24 }}>
      <Skeleton height={32} width="60%" />
      <div style={{ marginTop: 16 }}>
        <Skeleton height={80} />
      </div>
      <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
        {[1,2,3,4,5].map(i => <Skeleton key={i} height={100} style={{ flex: 1 }} />)}
      </div>
    </div>
  );
}

export default SkeletonCard;
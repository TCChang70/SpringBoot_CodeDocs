import useFetch from './useFetch';

function PostList() {
  const { data: posts, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/posts'
  );

  if (loading) return <p>載入中...</p>;
  if (error) return <p>錯誤：{error}</p>;

  return (
    <ul>
      {posts.slice(0, 5).map(post => <li key={post.id}>{post.title}</li>)}
    </ul>
  );
}

export default PostList;
// Mock authentication API — replace with real API calls (e.g., fetch/axios) in production

/**
 * Simulate a login API call.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{ token: string, user: { email: string } }>}
 */
export async function loginAPI(email, password) {
  // Simulate network latency
  await new Promise((resolve) => setTimeout(resolve, 800));

  if (!email || !password) {
    throw new Error('電子郵件和密碼不得為空');
  }

  // Demo: reject a specific "wrong" password to demonstrate error handling
  if (password === 'wrong') {
    throw new Error('帳號或密碼錯誤，請重試');
  }

  // Simulate successful response
  return { token: 'mock-jwt-token', user: { email } };
}

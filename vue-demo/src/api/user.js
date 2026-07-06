import request from '@/utils/request'

export const loginApi = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export const getUserListApi = () => {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export const getStudentListApi = () => {
  return request({
    url: '/student/list',
    method: 'get'
  })
}

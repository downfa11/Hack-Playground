# Solve 서비스

우아한테크코스 프리코스 진행중에 유용할 거라 생각돼서 시작한 프로젝트입니다.



### 과제 전형 코딩테스트 서비스 for k8s

<br>

처음에는 ECR 이미지를 ECS-Fargate로 실행하는 방식을 계획하고 시작했다.

그러나 Kubernetes API에 접근해서 가상화된 애플리케이션을 컨테이너 단위로 관리하는게 더 효율적이라 판단해서 개선중

<br>

---


## 1. 프로젝트 구성

### Business Flow

1. 과제 참여자의 과제 제출시 Github Repository를 Clone
2. 컨테이너 이미지로 빌드하여 ECR에 Push, Kubernetes 환경에서 프로비저닝
3. 입력값을 평가해서 데이터베이스에 사용자의 결과를 기록

<br>

### Business Model

- CTF 문제나 알고리즘 문제의 경우, 실시간으로 Pod를 켜두거나 노드 수가 많이 필요하기에 지속적인 비용 산출
- 과제 전형 같은 경우는 공개/비공개 과제의 총 참여자, 기간에 따른 비용 산출

<br>

### User Role

**문제 출제자** :

과제 내용에 해당하는 문제 내용을 필수적으로 작성해야한다. (과제 케이스, 제출 기한 등)

기본적으로 제공해야하는 템플릿이 존재하는 경우 Github Repository를 함께 제공해서 참여자가 Clone할 수 있도록 해야한다.


- [ ] 선택 - `Google SMTP Mail`을 이용해서 `identifier Id` 인증된 사용자만 접속
- [ ] 과제 참여자들의 현황 모니터링

<br>

**과제 참여자** :

문제 유형에 따라 회고록이나 느낀점, 제출할 Git 주소를 동봉해서 제출해야한다.

- [x] 해당 과제 열람, 제출하기, 과제 수정하기 (제출 기한 이내인 경우)
- [ ] 해당 참여자가 진행중인 과제 현황 (제출한 과제, 미제출한 과제 - 남은 제출 기한)
- [ ] `A third-party Github application`로 사용자의 Github에 간편하게 접근

<br>

### Assignment Case
문제 출제자는 여러 개의 입력값에 따른 결과값을 미리 제공해야한다.
- [x] 각 종류별 케이스에 따른 가중치도 문제 출제자가 지정할 수 있다.

<br>

문제의 케이스 종류는 히든, 평가, 예제로 구분된다.
- [x] 과제 참여자가 점수를 볼 수 있는 예제 케이스
   - 문제 형태에 맞춰서 잘 제출되었는지 확인하는 용도 
- [x] 실제 평가를 위한 평가, 히든 케이스

<br>

**format**

```jsx
[
    {
        "kind": "HIDDEN",
        "input": ["inputCase1","inputCase2"],
        "expectedOutput": ["expectedOutput1 \n expectedOutput2","expectedOutput3"],
        "bias": 30
    },
    {
        "kind": "HIDDEN",
        "input": ["inputCase3","inputCase4"],
        "expectedOutput": ["expectedOutput4","expectedOutput5"],
        "bias": 20
    }
]
```

각 케이스의 유형별로 가중치 합에 대한 비를 계산해야한다.

위 예시의 경우는 각 가중치가 3:2로 전체 문제에서 60%, 40% 비율을 차지해야한다.


<br>

## 2. 예상되는 문제점

여러 개의 ECR과 Fargate를 통한 컨테이너 환경 구성으로 드는 리소스 비용 문제

코드 실행 환경을 안전하게 설정하기 위한 보안 이슈(코드 실행시 제한된 권한 등)

- 입력된 코드가 과도한 시간이나 리소스를 낭비하지 않도록 제한

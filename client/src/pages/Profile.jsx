import { Row, Col, Tab, Tabs, Button, Image  } from 'react-bootstrap';
import { ReactComponent as EditBanner } from '../assets/icons/edit_Banner.svg';
import { API_SERVER, SERVER_URL } from '../config';
import { useAuth } from '../components/AuthContext';


import Trophy from '../assets/icons/pointsTrophy.svg';
import BornIn from '../assets/icons/bornIn.svg';
import Friends from '../assets/icons/friendsIcon.svg';
import Subscriptions from '../assets/icons/subscriptionsIcon.svg';
import Photos from '../assets/icons/photosIcon.svg';
import Favorite from '../assets/icons/favorite.svg';
import Calendar from '../assets/icons/calendar.svg';
import PostMenu from '../assets/icons/post-menu.svg';
import like from '../assets/icons/like.svg';
import comment from '../assets/icons/comment.svg';
import share from '../assets/icons/share.svg';

import imagePlaceholder from '../assets/icons/image-placeholder.svg';

function Profile() {
    const { setAuthUser, authUser } = useAuth();
    return (
        <Col className='wrapper-profile'>
            <Row className="profile-banner">
                <Button className='profile-btn'>
                    <EditBanner />
                </Button>
            </Row>
            <Col className='profile-header-list-container'>
                <Tabs defaultActiveKey="Posts" className="profile-header-list">
                    <Tab eventKey="Posts" title={<Col><p>Posts</p><p><b>13</b></p></Col>}>
                    </Tab>
                    <Tab eventKey="Likes" title={<Col><p>Likes</p><p><b>13</b></p></Col>}>
                        {/* <p>Likes</p>
                        <p><b>255</b></p> */}
                    </Tab>
                    {/* <Tab eventKey="Answers" title={<Col><p>Answers</p><p><b>13</b></p></Col>}>
                        <p>Answers</p>
                        <p><b>13</b></p>
                    </Tab>
                    <Tab eventKey="Questions" title={<Col><p>Questions</p><p><b>13</b></p></Col>}>
                        <p>Questions</p>
                        <p><b>13</b></p>
                    </Tab> */}
                    <Tab eventKey="Bookmarks" title={<Col><p>Bookmarks</p><p><b>13</b></p></Col>}>
                        {/* <p>Bookmarks</p>
                        <p><b>2</b></p> */}
                    </Tab>
                </Tabs>
            </Col>
            <Row className='d-flex' style={{fontFamily: 'Manrope', fontWeight: '600', width: 'fit-content'}}>
                <Row className='d-flex flex-column info-profile'>
                    <Row>
                        <Image
                            src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                            style={{ padding: 'unset' }}
                            className='profile-avatar'
                            alt='Avatar'
                            roundedCircle
                        />
                    </Row>
                    <Row>
                        <p className='nickname'>Durgesh Kirillovich</p>
                        <Col className='d-flex flex-row w-auto'>
                            <p style={{color: '#8F8F8F', fontSize: '18px', paddingRight: '7px', fontWeight: '400'}}>@durgesh</p>
                            <Image
                                src={Trophy}
                                style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                            <p style={{color: '#3D00FF', fontSize: '18px'}}>19k points</p>
                        </Col>
                    </Row>
                    <Row className="marginTopDates" style={{color: '#8F8F8F', fontSize: '13px'}}>
                        <Col className='d-flex flex-row'>
                            <Image
                                src={Calendar}
                                style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                            <p style={{ fontWeight: '500'}}>Joined in September 2023</p>
                        </Col>
                        <Col className='d-flex flex-row'>
                            <Image
                                src={BornIn}
                                style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                            <p style={{ fontWeight: '500'}}>Born in December 22</p>
                        </Col>
                    </Row>
                    <hr></hr>
                    <Row>
                        <p style={{color: '#373737', paddingLeft: '0px'}}>Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!</p>
                        <Button variant="secondary" style={{fontFamily:'Manrope', fontWeight:'bold', marginTop: '10px'}}>Edit profile</Button>{' '}
                    </Row>
                    <Row className='profile-links'>
                        <Col className='d-flex flex-row mb-1'>
                            <Image
                                src={Friends}
                                style={{ padding: 'unset', paddingRight: '8px'}}
                            />
                            <p><span>24</span> • <a href="/">friends</a></p>
                        </Col>
                        <Col className='d-flex flex-row my-1'>
                            <Image
                                src={Favorite}
                                style={{ padding: 'unset', paddingRight: '8px'}}
                            />
                            <p><span>15</span> • <a href="/">followers</a></p>
                        </Col>
                        <Col className='d-flex flex-row my-1'>
                            <Image
                                src={Subscriptions}
                                style={{ padding: 'unset', paddingRight: '8px'}}
                            />
                            <p><span>34</span> • <a href="/">subscriptions</a></p>
                        </Col>
                        <Col className='d-flex flex-row my-1'>
                            <Image
                                src={Photos}
                                style={{ padding: 'unset', paddingRight: '8px'}}
                            />
                            <p><span>7</span> • <a href="/">photos and videos</a></p>
                        </Col>
                        <Col className='d-flex flex-row mt-1 photos-videos-block'>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                            <Col>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Col>
                        </Col>
                    </Row>
                </Row>
                <Row className="main-block-profile tab-content">
                    <Row className='post-block'>
                        <Image
                            src={PostMenu}
                            style={{ padding: 'unset', position:'absolute', width:'19px', right:'19px', top:'25px'}}
                        /> 
                        <Col className='post-user'>
                            <Row className='post-user-img'>
                                <Image
                                    src={authUser.avatarUrl ? `${SERVER_URL}/${authUser.avatarUrl}` : imagePlaceholder}
                                />
                            </Row>
                            <Row>
                                <p><span style={{color: 'black', fontWeight: 'bold'}}>Durgesh Kirillovich</span> • <span>@durgesh</span></p>
                                <span style={{ display: 'flex', alignItems: 'center'}}>
                                    <Image
                                        src={Calendar}
                                        style={{ padding: 'unset', paddingRight: '7px'}}
                                    />
                                    20 september
                                </span>
                            </Row>
                        </Col>
                        <Col className='post-main-block'>
                            <p>♥ Such a beautiful  day ♥ <br></br>Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!Explorer of art, nature, and cultures. Travel enthusiast, book lover, and social good advocate. Let's connect and share experiences!</p>
                        </Col>
                        <Col>
                            <Image
                                src={like}
                                // style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                            <Image
                                src={comment}
                                // style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                            <Image
                                src={share}
                                // style={{ padding: 'unset', paddingRight: '7px'}}
                            />
                        </Col>
                    </Row>
                </Row>
            </Row>
        </Col>
    );
}

export default Profile;